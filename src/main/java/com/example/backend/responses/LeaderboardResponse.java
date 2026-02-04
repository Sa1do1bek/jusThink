package com.example.backend.responses;

import java.util.List;

public record LeaderboardResponse(
        List<PlayerStatsResponse> playerStats
) {
}
