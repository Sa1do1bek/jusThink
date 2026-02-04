package com.example.backend.security.ws_security;

import com.example.backend.enums.SessionRole;
import com.example.backend.repositories.PlayerRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        String userIdStr = (String) attributes.get("userId");
        SessionRole role = (SessionRole) attributes.get("role");

        if (userIdStr == null || role == null) {
            return null;
        }

        UUID userId = UUID.fromString(userIdStr);

        if (role == SessionRole.PLAYER) {
            return playerRepository.findById(userId)
                    .map(PlayerPrincipal::new)
                    .orElse(null);
        }

        if (role == SessionRole.HOST) {
            return userRepository.findById(userId)
                    .map(HostPrincipal::new)
                    .orElse(null);
        }

        return null;
    }

}

