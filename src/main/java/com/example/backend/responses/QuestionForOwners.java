package com.example.backend.responses;

import com.example.backend.enums.QuestionType;

import java.util.List;
import java.util.UUID;

public record QuestionForOwners(
        UUID id,
        String text,
        int score,
        int timeInSeconds,
        QuestionType questionType,
        List<OptionsForOwners> options
) {
}
