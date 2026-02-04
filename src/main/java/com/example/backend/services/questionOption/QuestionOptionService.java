package com.example.backend.services.questionOption;

import com.example.backend.models.QuestionOption;
import com.example.backend.repositories.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionOptionService implements IQuestionOptionService{

    private final QuestionOptionRepository repository;

    @Override
    public QuestionOption getQuestionOptionById(UUID questionOptionId) {
        return repository.findById(questionOptionId)
                .orElse(null);
    }
}