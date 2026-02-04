package com.example.backend.controllers;

import com.example.backend.enums.SessionRole;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Session;
import com.example.backend.requests.CreatePlayerAnswerRequest;
import com.example.backend.requests.CreateSessionRequest;
import com.example.backend.responses.ApiResponse;
import com.example.backend.responses.PlayerAnswerResponse;
import com.example.backend.responses.SessionResponse;
import com.example.backend.responses.TokenSessionResponse;
import com.example.backend.services.jwt.JwtService;
import com.example.backend.services.session.SessionService;
import com.example.backend.services.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/sessions")
public class    SessionController {

    private final SessionService sessionService;
    private final WebSocketService webSocketService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtService jwtService;

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN', 'USER')")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> getSessionById(@PathVariable UUID sessionId) {
        try {
            Session session = sessionService.getSessionById(sessionId);
            SessionResponse sessionResponse = sessionService.converterToSessionResponse(session);

            return ResponseEntity.ok(new ApiResponse("Session found!", sessionResponse));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllSessions() {
        try {
            List<Session> sessions = sessionService.getAllSessions();
            List<SessionResponse> responseList= sessions
                    .stream()
                    .map(sessionService::converterToSessionResponse)
                    .toList();

            return ResponseEntity.ok(new ApiResponse("All sessions!", responseList));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> createSession(@RequestBody CreateSessionRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Session session = sessionService.createSession(request, email);
            SessionResponse sessionResponse = sessionService.converterToSessionResponse(session);
            String token = jwtService.generateWSToken(session.getHost().getId(), SessionRole.HOST);
            System.out.println("\n\n\ntoken: " + token + "\n\n\n");

            return ResponseEntity.ok(new ApiResponse(
                    "Session created successfully!",
                    new TokenSessionResponse(sessionResponse, token)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/{sessionId}/start")
    public void startSession(@PathVariable UUID sessionId) {
        try {
            webSocketService.startSession(sessionId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/skip")
    public void skipCurrentQuestion(@PathVariable UUID sessionId) {
        try {
            webSocketService.skipQuestion(sessionId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/end")
    public void endSession(@PathVariable UUID sessionId) {
        try {
            webSocketService.endSession(sessionId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @MessageMapping("/session/{sessionId}/answer")
    public void receiveAnswer(@DestinationVariable UUID sessionId,
                              @Payload CreatePlayerAnswerRequest request,
                              Principal principal) {
        try {
            PlayerAnswerResponse response = webSocketService.submitAnswer(sessionId, request);

            System.out.println("\n\nanswer player id: " + request.playerId() + "\n\n");
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/feedback",
                    response
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}