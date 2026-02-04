package com.example.backend.services.playerAnswers;

import com.example.backend.requests.CreatePlayerAnswerRequest;

import java.util.UUID;

public interface IPlayerAnswerService {
    void createPlayerAnswer(
            UUID sessionId,
            CreatePlayerAnswerRequest request,
            int score,
            int timeTakenSeconds
    );
}
