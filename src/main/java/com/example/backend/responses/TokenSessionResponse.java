package com.example.backend.responses;

public record TokenSessionResponse(
        SessionResponse sessionResponse,
        String token
) {
}
