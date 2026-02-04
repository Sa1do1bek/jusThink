package com.example.backend.responses;

public record QuestionAverageTime(
        Integer orderNumber,
        String text,
        Integer timeInSeconds,
        Double timeAnswered
) {
}
