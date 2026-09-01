package com.logstream.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logstream.backend.grpc.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LogBroadcaster {

    // Thread-safe set of currently connected clients
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void register(WebSocketSession session) {
        sessions.add(session);

        log.info(
                "WebSocket client connected: {} (total: {})",
                session.getId(),
                sessions.size()
        );
    }

    public void unregister(WebSocketSession session) {
        sessions.remove(session);

        log.info(
                "WebSocket client disconnected: {} (total: {})",
                session.getId(),
                sessions.size()
        );
    }

    public void broadcast(LogMessage logMessage) {

        if (sessions.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();

            payload.put("log_id", logMessage.getLogId());
            payload.put("timestamp", logMessage.getTimestamp());
            payload.put("level", logMessage.getLevel());
            payload.put("service", logMessage.getService());
            payload.put("message", logMessage.getMessage());
            payload.put(
                    "response_time_ms",
                    logMessage.getResponseTimeMs()
            );

            String json = objectMapper.writeValueAsString(payload);

            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }

        } catch (IOException e) {
            log.error("Failed to broadcast log via WebSocket", e);
        }
    }
}
