package com.example.backend.services.player;

import com.example.backend.models.Player;
import com.example.backend.requests.CreatePlayerRequest;
import com.example.backend.responses.PlayerResponse;

import java.util.Optional;
import java.util.UUID;

public interface IPlayerService {
    Player createPlayer(CreatePlayerRequest request);
    void deletePlayer(UUID playerId);
    PlayerResponse converterToPlayerResponse(Player player);
    Player getPlayerById(UUID playerId);
}
