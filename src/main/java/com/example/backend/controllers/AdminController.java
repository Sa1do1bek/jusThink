package com.example.backend.controllers;

import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Quiz;
import com.example.backend.responses.ApiResponse;
import com.example.backend.responses.QuizResponse;
import com.example.backend.services.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin")
public class AdminController {

    private final QuizService quizService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse> getQuizById(@PathVariable UUID quizId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Quiz found!",
                    quizService.converterToQuizVersionResponse(quizService.getQuizByIdForAdmin(
                            quizId,
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName()))
                    )
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/quizzes")
    public ResponseEntity<ApiResponse> getQuizAll() {
        try {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            List<QuizResponse> quizResponses = quizzes.stream()
                    .map(quizService::converterToQuizVersionResponse)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All quizzes!", quizResponses));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}