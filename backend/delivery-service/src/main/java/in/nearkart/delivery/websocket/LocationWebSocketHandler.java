package in.nearkart.delivery.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.service.LiveLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private final LiveLocationService locationService;
    private final ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String partnerId = extractPartnerId(session);
        sessions.put(partnerId, session);
        log.info("WebSocket connected: partnerId={}", partnerId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String partnerId = extractPartnerId(session);
        UpdateLocationRequest req = objectMapper.readValue(message.getPayload(), UpdateLocationRequest.class);
        locationService.updateLocation(UUID.fromString(partnerId), req);
        session.sendMessage(new TextMessage("{\"status\":\"location_updated\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String partnerId = extractPartnerId(session);
        sessions.remove(partnerId);
        log.info("WebSocket disconnected: partnerId={}, status={}", partnerId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    private String extractPartnerId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    public void broadcastToSession(String targetPartnerId, String payload) {
        WebSocketSession target = sessions.get(targetPartnerId);
        if (target != null && target.isOpen()) {
            try {
                target.sendMessage(new TextMessage(payload));
            } catch (Exception e) {
                log.error("Failed to broadcast to {}: {}", targetPartnerId, e.getMessage());
            }
        }
    }
}
