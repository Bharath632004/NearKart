package in.nearkart.delivery.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.service.LiveLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time delivery partner location updates.
 * URL: /ws/tracking/{assignmentId}
 *
 * Partners connect and push location JSON:
 *   { "latitude": 17.5, "longitude": 78.5, "speedKmph": 25.0 }
 *
 * Customers/merchants connect to receive live location broadcasts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private final LiveLocationService locationService;
    private final ObjectMapper objectMapper;

    /** assignmentId → set of subscriber sessions */
    private final Map<String, Map<String, WebSocketSession>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String assignmentId = extractAssignmentId(session);
        subscribers.computeIfAbsent(assignmentId, k -> new ConcurrentHashMap<>())
                   .put(session.getId(), session);
        log.info("WebSocket connected: sessionId={}, assignmentId={}", session.getId(), assignmentId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String assignmentId = extractAssignmentId(session);
        // Parse location update sent by delivery partner
        UpdateLocationRequest locationReq = objectMapper.readValue(
                message.getPayload(), UpdateLocationRequest.class);

        // TODO: Extract real partnerId from JWT in WebSocket handshake headers
        UUID partnerId    = UUID.randomUUID(); // placeholder
        UUID assignmentUUID = UUID.fromString(assignmentId);

        locationService.updateLocation(partnerId, assignmentUUID, locationReq);

        // Broadcast location to all subscribers on this assignment
        broadcastToAssignment(assignmentId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String assignmentId = extractAssignmentId(session);
        Map<String, WebSocketSession> sessions = subscribers.get(assignmentId);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) subscribers.remove(assignmentId);
        }
        log.info("WebSocket disconnected: sessionId={}", session.getId());
    }

    private void broadcastToAssignment(String assignmentId, String payload) {
        Map<String, WebSocketSession> sessions = subscribers.getOrDefault(
                assignmentId, Map.of());
        sessions.values().forEach(s -> {
            try {
                if (s.isOpen()) s.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                log.warn("Failed to broadcast to session {}: {}", s.getId(), e.getMessage());
            }
        });
    }

    private String extractAssignmentId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
