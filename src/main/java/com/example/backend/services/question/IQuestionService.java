package com.example.backend.services.question;

import com.example.backend.models.Question;
import com.example.backend.responses.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface IQuestionService {
    Question getQuestionById(UUID questionId);
//    QuestionResponse converterToQuestionResponse(Question question);
    List<UUID> getAllQuestionIdsByQuizVersion(UUID quizVersionId);
}
