package com.example.backend.responses;

import java.util.UUID;

public record PlayerDto(
        UUID playerId,
        String nickName
) {
}
