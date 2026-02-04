package com.example.backend.responses;

public record TokenPlayerResponse(
        PlayerDto player,
        String token
) {
}
