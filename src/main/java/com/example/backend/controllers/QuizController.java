package com.example.backend.controllers;

import com.example.backend.enums.QuizMode;
import com.example.backend.models.Quiz;
import com.example.backend.requests.CreateQuizRequest;
import com.example.backend.requests.UpdateQuizRequest;
import com.example.backend.responses.ApiResponse;
import com.example.backend.responses.QuizResponse;
import com.example.backend.responses.QuizSafeResponse;
import com.example.backend.services.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/quizzes")
public class QuizController {

    private final QuizService quizService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> createQuiz(@RequestBody CreateQuizRequest request) {
        try {
            Quiz quiz = quizService.createQuiz(request, getCurrentUserEmail());
            return ResponseEntity.ok(new ApiResponse(
                    "Quiz created successfully!",
                    quizService.converterToQuizVersionResponse(quiz)
            ));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse> updateQuiz(@PathVariable UUID quizId, @RequestBody UpdateQuizRequest request) {
        try {
            Quiz quiz = quizService.updateQuiz(quizId, request, getCurrentUserEmail());
            QuizResponse response = quizService.converterToQuizVersionResponse(quiz);
            return ResponseEntity.ok(new ApiResponse("Quiz updated successfully!", response));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse> deleteQuiz(@PathVariable UUID quizId) {
        try {
            quizService.deleteQuiz(quizId, getCurrentUserEmail());
            return ResponseEntity.ok(new ApiResponse("Quiz archived successfully!", null));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'USER')")
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse> getQuizById(@PathVariable UUID quizId) {
        try {
            Object quiz = quizService.getQuizById(quizId, getCurrentUserEmail());
            return ResponseEntity.ok(new ApiResponse("Quiz found!", quiz));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<ApiResponse> getQuizAllPublished() {
        try {
            List<Quiz> quizzes = quizService.getAllQuizzesForUsers();
            List<QuizSafeResponse> responses = quizzes.stream()
                    .map(quizService::converterToQuizSafeResponse)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All published quizzes!", responses));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/by-mode/{quizId}")
    public ResponseEntity<ApiResponse> getQuizByIdAndMode(@PathVariable UUID quizId, @RequestParam QuizMode mode) {
        try {
            Object quiz = quizService.getQuizByIdAndMode(quizId, mode, getCurrentUserEmail());
            return ResponseEntity.ok(new ApiResponse("Quiz found!", quiz));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse(message, null));
    }
}
