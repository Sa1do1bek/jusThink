package com.example.backend.services.quiz;

import com.example.backend.enums.QuizMode;
import com.example.backend.models.Quiz;
import com.example.backend.requests.CreateQuizRequest;
import com.example.backend.responses.QuizResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IQuizService {
    Object getQuizById(UUID id, String email);
    List<Quiz> getAllQuizzes();
    List<Quiz> getAllQuizzesForUsers();
    Object getQuizByIdAndMode(UUID id, QuizMode mode, String email);
    Quiz createQuiz(CreateQuizRequest request, String userEmail, MultipartFile imageFile);
    QuizResponse converterToQuizVersionResponse(Quiz quiz);
}
