package com.example.backend.responses;

import java.util.UUID;

public record PlayerFinalStats(
        UUID playerId,
        String nickname,
        Long score,
        Long correctQuestionsNumber
) {
}
