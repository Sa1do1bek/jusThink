package com.example.backend.services.questionOption;

import com.example.backend.models.QuestionOption;

import java.util.UUID;

public interface IQuestionOptionService {
    QuestionOption getQuestionOptionById(UUID questionOptionId);
}
