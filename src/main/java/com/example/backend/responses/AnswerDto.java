package com.example.backend.responses;

import java.util.UUID;

public record AnswerDto(
        UUID playerId,
        long score
) {
}
