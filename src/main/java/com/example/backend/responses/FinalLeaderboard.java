package com.example.backend.responses;

import java.util.List;

public record FinalLeaderboard(
    Long totalQuestionsNumber,
    List<PlayerFinalStats> playerFinalStats
) {
}
