package com.example.backend.requests;


public record CreateQuestionOption(
        String text,
        boolean isCorrect,
        int optionOrder
) {
}
