package com.example.backend.responses;

import java.util.UUID;

public record PlayerJoinResponse(
        UUID playerId,
        String token
) {
}
