package com.example.backend.requests;

import com.example.backend.enums.QuestionType;
import com.example.backend.enums.QuizMode;
import com.example.backend.enums.QuizStatus;

import java.util.List;

public record CreateQuizRequest(
        String title,
        String description,
        QuizStatus status,
        QuizMode mode,
        List<CreateQuestionRequest> questions
) {

}
