package com.example.backend.responses;

public record OptionsCountResponse(
        Integer optionOrder,
        Long count,
        Boolean isCorrect
) {
}