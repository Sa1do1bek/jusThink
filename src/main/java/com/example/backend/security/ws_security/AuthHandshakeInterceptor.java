package com.example.backend.security.ws_security;

import com.example.backend.enums.SessionRole;
import com.example.backend.repositories.PlayerRepository;
import com.example.backend.responses.UserInfoWS;
import com.example.backend.services.jwt.JwtService;
import com.example.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final PlayerRepository playerRepository;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        HttpServletRequest servlet = servletRequest.getServletRequest();
        String token = servlet.getParameter("token");

        if (token == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        UserInfoWS userInfo;

        try {
            userInfo = jwtService.validateAndGetUserInfo(token);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        UUID userId = userInfo.userId();
        SessionRole role = userInfo.role();

        if (!isValidUser(role, userId)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("userId", userInfo.userId().toString());
        attributes.put("role", userInfo.role());

        return true;
    }

    private boolean isValidUser(SessionRole role, UUID userId) {
        return switch (role) {
            case HOST -> userService.getUserById(userId) != null;
            case PLAYER -> playerRepository.findById(userId).isPresent();
        };
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {}
}