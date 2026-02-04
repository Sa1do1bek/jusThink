package com.example.backend.requests;

import java.time.Instant;
import java.util.UUID;

public record CreatePlayerAnswerRequest(
        UUID playerId,
        UUID questionId,
        UUID questionOptionId,
        Instant answeredTime
) {
}
