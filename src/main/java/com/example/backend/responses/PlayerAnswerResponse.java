package com.example.backend.responses;

public record PlayerAnswerResponse(
        boolean isCorrect,
        int score
) {
}
