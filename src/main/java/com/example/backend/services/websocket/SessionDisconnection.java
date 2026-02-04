package com.example.backend.services.websocket;

import com.example.backend.models.Session;
import com.example.backend.services.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionDisconnection implements ApplicationListener<SessionDisconnectEvent> {

    private final SessionService sessionService;
    private final WebSocketService webSocketService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String stompSessionId = event.getSessionId();

        Session session = sessionService.removeStompSessionId(stompSessionId);
        if (session == null) return;
        webSocketService.endSession(session.getId());

        System.out.println("Session " + session.getId() + " disconnected with STOMP session: " + stompSessionId);
    }
}
