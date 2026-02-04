package com.example.backend.services.websocket;

import com.example.backend.services.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionConnection implements ApplicationListener<SessionConnectEvent> {

    private final SessionService sessionService;

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String stompSessionId = accessor.getSessionId();
        System.out.println("Stomp id: " + stompSessionId);

        String sessionIdHeader = accessor.getFirstNativeHeader("sessionId");
        if (sessionIdHeader == null) return;

        UUID sessionId = UUID.fromString(sessionIdHeader);
        sessionService.saveStompSessionId(sessionId, stompSessionId);

        System.out.println("\n\n\nSession " + sessionId + " connected with STOMP session: " + stompSessionId + "\n\n\n");
    }
}
