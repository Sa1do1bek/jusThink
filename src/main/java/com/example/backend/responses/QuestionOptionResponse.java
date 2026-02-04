package com.example.backend.responses;

import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        Integer optionOrder,
        String text
) {
}
