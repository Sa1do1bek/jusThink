package com.example.backend.responses;

public record PlayerStatsResponse(
        String nickName,
        long score
) {
}
