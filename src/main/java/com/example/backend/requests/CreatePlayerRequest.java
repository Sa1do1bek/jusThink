package com.example.backend.requests;

public record CreatePlayerRequest(
        String sessionCode,
        String nickName
) {
}
