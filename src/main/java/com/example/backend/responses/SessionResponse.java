package com.example.backend.responses;

import com.example.backend.enums.SessionMode;
import com.example.backend.enums.SessionStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String sessionCode,
        SessionStatus status,
        SessionMode mode,
        UserResponse host,
        QuizVersionResponse quizVersion,
        List<PlayerResponse> players,
        LocalDate startedAt,
        LocalDate endedAt
) {
}
