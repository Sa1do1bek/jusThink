package com.example.backend.responses;

import java.util.List;

public record PlayersJoinResponse(
        PlayerDto newPlayers,
        List<PlayerDto> players
) {
}
