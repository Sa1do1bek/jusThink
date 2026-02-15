package com.example.backend.services.ai;

import com.example.backend.requests.CreateQuizRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LLMService {

    @Value("${groq.api-url}")
    private String apiUrl;
    @Value("${groq.model}")
    private String model;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public CreateQuizRequest generateQuiz(String topic) {

        String systemPrompt = """
            You are a quiz generation engine.
    
            You MUST return ONLY valid raw JSON.
            Do NOT include explanations.
            Do NOT include markdown.
            Do NOT include text outside JSON.
        
            Keep title and description under 10 words.
            Keep question text under 15 words.
            Keep option text under 8 words.
            Use short, concise wording.
        
            Avoid long explanations.
            Do not repeat phrases.
    
            Return ONLY valid raw JSON matching:
        
            {
             "title": string,
             "description": string,
             "questions": [
               {
                 "text": string,
                 "questionType": "SINGLE_CHOICE"
                 "options": [
                   { "text": string, "isCorrect": boolean}
                 ]
               }
             ]
            }
    
            Rules:
            - 10 questions.
            - questionType must be SINGLE_CHOICE.
            - 4 options per question.
            - 1 option must have isCorrect = true.
            - concise wording
        """;

        String userPrompt = "Generate a quiz about: " + topic;

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.2
        );

        return parseQuizResponse(
                webClient.post()
                        .uri(apiUrl)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
        );
    }

    public CreateQuizRequest parseQuizResponse(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            content = content.replace("```json", "")
                    .replace("```", "")
                    .trim();

            return objectMapper.readValue(content, CreateQuizRequest.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI quiz response", e);
        }
    }
}
