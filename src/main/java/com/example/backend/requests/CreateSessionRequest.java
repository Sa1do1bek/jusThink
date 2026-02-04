package com.example.backend.requests;

import com.example.backend.enums.SessionStatus;

import java.util.UUID;

public record CreateSessionRequest(
        UUID quizId,
        SessionStatus status
) {
}
