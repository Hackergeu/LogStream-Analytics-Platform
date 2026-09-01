package com.logstream.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class LogWebSocketHandler extends TextWebSocketHandler {

    private final LogBroadcaster broadcaster;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        broadcaster.register(session);
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        broadcaster.unregister(session);
    }
}
