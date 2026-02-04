package com.example.backend.requests;

import com.example.backend.enums.QuestionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record UpdateQuestionRequest(
        Optional<UUID> id,
        Optional<String> text,
        Optional<List<UpdateQuestionOption>> options,
        Optional<QuestionType> questionType,
        Optional<Integer> timeInSeconds,
        Optional<Integer> score
) {
}
