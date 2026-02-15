package com.example.backend.controllers.user;

import com.example.backend.responses.ApiResponse;
import com.example.backend.services.ai.LLMService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/ai")
public class AiController {

    private final LLMService llmService;

    public AiController(LLMService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/quiz")
    public ResponseEntity<ApiResponse> ask(@RequestParam String topic) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "AI output for quiz creation!",
                    llmService.generateQuiz(topic)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}