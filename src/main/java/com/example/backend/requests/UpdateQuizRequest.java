package com.example.backend.requests;

import com.example.backend.enums.QuizMode;
import com.example.backend.enums.QuizStatus;

import java.util.List;
import java.util.Optional;

public record UpdateQuizRequest(
        Optional<String> title,
        Optional<String> description,
        Optional<QuizStatus> status,
        Optional<QuizMode> mode,
        Optional<List<UpdateQuestionRequest>> questions
) {
}
