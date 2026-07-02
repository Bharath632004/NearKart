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

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private final LiveLocationService liveLocationService;
    private final ObjectMapper objectMapper;

    // partnerId (UUID string) -> WebSocketSession
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String partnerId = getPartnerId(session);
        sessions.put(partnerId, session);
        log.info("WebSocket connected: partnerId={}", partnerId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String partnerIdStr = getPartnerId(session);
        UpdateLocationRequest request = objectMapper.readValue(message.getPayload(), UpdateLocationRequest.class);
        // Fix: parse as UUID instead of Long
        liveLocationService.updateLocation(UUID.fromString(partnerIdStr), request);
        session.sendMessage(new TextMessage("{\"status\":\"location updated\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String partnerId = getPartnerId(session);
        sessions.remove(partnerId);
        log.info("WebSocket disconnected: partnerId={}", partnerId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket error for session {}: {}", session.getId(), exception.getMessage());
    }

    public static void sendToPartner(String partnerId, String message) {
        WebSocketSession session = sessions.get(partnerId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("Failed to send WS message to partnerId={}", partnerId, e);
            }
        }
    }

    private String getPartnerId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        if (query != null && query.contains("partnerId=")) {
            return query.split("partnerId=")[1].split("&")[0];
        }
        return session.getId();
    }
}
