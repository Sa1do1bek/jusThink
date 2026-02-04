package com.example.backend.controllers;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.responses.ApiResponse;
import com.example.backend.services.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> getSessionStats(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Session statistics!",
                    analyticsService.sessionAnalytics(
                            sessionId,
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName()))
            );
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/percentage/question/{sessionId}")
    public ResponseEntity<ApiResponse> getQuestionCorrectness(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(new ApiResponse("Session statistics!",
                    analyticsService.getCorrectnessPercentage(
                        sessionId,
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName()))
            );
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/percentage/session/{sessionId}")
    public ResponseEntity<ApiResponse> getSessionAnswerPercentage(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Session correct answer percentage!",
                    analyticsService.getSessionAnswerPercentage(
                        sessionId,
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName()))
            );
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/number/players/{sessionId}")
    public ResponseEntity<ApiResponse> getPlayersNumberPerSession(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Session players number!",
                    analyticsService.getPlayersNumberPerSession(
                            sessionId,
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName()))
            );
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/percentage/answer")
    public ResponseEntity<ApiResponse> getAllAnswerPercentage() {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "All correct answer percentage in platform!",
                    analyticsService.getAllAnswerPercentage())
            );
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/number/session")
    public ResponseEntity<ApiResponse> getNumberSession() {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "All correct answer percentage in platform!",
                    analyticsService.getNumberSession())
            );
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Authentication failed", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/time/session/{sessionId}")
    public ResponseEntity<ApiResponse> getSessionAnswerAverageTime(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Average time to answer in session!",
                    analyticsService.getSessionAnswerAverageTime(
                            sessionId,
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName()))
            );
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/time/question/{sessionId}")
    public ResponseEntity<ApiResponse> getQuestionAnswerAverageTime(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Average time to answer per question in session!",
                    analyticsService.getQuestionAnswerAverageTime(
                            sessionId,
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName()))
            );
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}