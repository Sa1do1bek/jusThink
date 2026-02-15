package com.example.backend.controllers.user;

import com.example.backend.enums.QuizMode;
import com.example.backend.models.Quiz;
import com.example.backend.requests.CreateQuizRequest;
import com.example.backend.requests.UpdateQuizRequest;
import com.example.backend.responses.ApiResponse;
import com.example.backend.responses.QuizResponse;
import com.example.backend.responses.QuizSafeResponse;
import com.example.backend.services.image.ImageStorageService;
import com.example.backend.services.quiz.QuizService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final ImageStorageService storage;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PreAuthorize("hasAuthority('quiz:create')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> createQuiz(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        try {
            Quiz quiz = quizService.createQuiz(
                    new ObjectMapper().readValue(requestJson, CreateQuizRequest.class),
                    getCurrentUserEmail(),
                    imageFile
            );
            return ResponseEntity.ok(new ApiResponse(
                    "Quiz created successfully!",
                    quizService.converterToQuizVersionResponse(quiz)
            ));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAuthority('quiz:update')")
    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse> updateQuiz(
            @PathVariable UUID quizId,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        try {
            Quiz quiz = quizService.updateQuiz(quizId,
                    new ObjectMapper().readValue(requestJson, UpdateQuizRequest.class),
                    getCurrentUserEmail(),
                    imageFile
            );
            QuizResponse response = quizService.converterToQuizVersionResponse(quiz);
            return ResponseEntity.ok(new ApiResponse("Quiz updated successfully!", response));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAuthority('image-by-quiz-id:delete')")
    @DeleteMapping("/{quizId}/image")
    public ResponseEntity<ApiResponse> deleteImageByQuizId(@PathVariable UUID quizId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            quizService.deleteImageByQuizId(quizId, email);
            return ResponseEntity.ok(new ApiResponse("Image deleted successfully!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('quiz:delete')")
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse> deleteQuiz(@PathVariable UUID quizId) {
        try {
            quizService.deleteQuiz(quizId, getCurrentUserEmail());
            return ResponseEntity.ok(new ApiResponse("Quiz archived successfully!", null));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('image-quiz:get')")
    @GetMapping("/media/images/image/{path}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String path) {
        try {
            return storage.loadImage(path)
                    .map(resource ->
                            ResponseEntity.ok()
                                    .contentType(MediaType.IMAGE_PNG)
                                    .body(resource)
                    )
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAuthority('quiz-by-id:get')")
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse> getQuizById(@PathVariable UUID quizId) {
        try {
            Object quiz = quizService.getQuizById(quizId, getCurrentUserEmail());
            return ResponseEntity.ok(new ApiResponse("Quiz found!", quiz));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('quiz-all-published:get')")
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

    @PreAuthorize("hasAuthority('quiz-by-id-and-mode:get')")
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
