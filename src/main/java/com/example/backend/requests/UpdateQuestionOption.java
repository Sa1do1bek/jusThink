package com.example.backend.requests;

import java.util.Optional;
import java.util.UUID;

public record UpdateQuestionOption(
        Optional<UUID> id,
        Optional<String> text,
        Optional<Boolean> isCorrect,
        Optional<Integer> optionOrder
) {
}
