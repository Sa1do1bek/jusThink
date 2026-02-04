package com.example.backend.responses;

import java.time.LocalDate;
import java.util.UUID;

public record QuizResponse(
        UUID id,
        String title,
        String description,
        UserResponse owner,
        LocalDate createdAt,
        QuizVersionResponse quizVersionResponse
) {
}
