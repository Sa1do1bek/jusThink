package com.example.backend.requests;


import com.example.backend.enums.QuestionType;

import java.util.List;

public record CreateQuestionRequest(
        String text,
        List<CreateQuestionOption> options,
        QuestionType questionType,
        int orderNumber,
        int timeInSeconds,
        int score
) {

}
