package com.example.backend.controllers;

import com.example.backend.enums.EventMessageType;
import com.example.backend.enums.SessionRole;
import com.example.backend.security.ws_security.StompPrincipal;
import com.example.backend.models.Player;
import com.example.backend.models.Session;
import com.example.backend.requests.CreatePlayerRequest;
import com.example.backend.responses.*;
import com.example.backend.services.jwt.JwtService;
import com.example.backend.services.player.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/players")
public class PlayerController {

    private final PlayerService playerService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ApiResponse> createPlayer(@RequestBody CreatePlayerRequest request) {
        try {
            PlayerDto player = playerService.converterToPlayerDto(
                    playerService.createPlayer(request)
            );
            String token = jwtService.generateWSToken(player.playerId(), SessionRole.PLAYER);

            return ResponseEntity.ok(new ApiResponse(
                    "Player created successfully!",
                    new TokenPlayerResponse(player, token))
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @MessageMapping("session/join")
    public void joinPlayer(Principal principal) {
        try {
            StompPrincipal p = (StompPrincipal) principal;
            if (!p.getRole().equals(SessionRole.PLAYER.name()))
                throw new AccessDeniedException("Only players can join sessions");

            Player player = playerService.getPlayerById(UUID.fromString(p.getName()));
            Session session = player.getSession();
            List<PlayerDto> players = playerService.getAllBySessionId(session.getId()).stream()
                    .map(playerService::converterToPlayerDto)
                    .toList();
            PlayerDto newPlayerResponse = playerService.converterToPlayerDto(player);
            messagingTemplate.convertAndSend(
                    "/topic/session/" + session.getId(),
                    new EventMessageResponse(
                            EventMessageType.PLAYER_JOINED,
                            "Player joined!",
                            new PlayersJoinResponse(newPlayerResponse, players)
                    )
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new EventMessageResponse(
                            EventMessageType.ERROR,
                            e.getMessage(),
                            null
                    )
            );
        }
    }
    @MessageMapping("session/leave")
    public void leavePlayer(Principal principal) {
        try {
            StompPrincipal p = (StompPrincipal) principal;
            UUID playerId = UUID.fromString(p.getName());

            Player player = playerService.getPlayerById(playerId);

            Session session = player.getSession();
            playerService.deletePlayer(playerId);

            List<PlayerResponse> players = session.getPlayers().stream()
                    .map(playerService::converterToPlayerResponse)
                    .toList();

            messagingTemplate.convertAndSend(
                    "/topic/session/" + session.getId(),
                    new EventMessageResponse(
                            EventMessageType.PLAYER_LEFT,
                            "Player left!",
                            players
                    )
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new EventMessageResponse(
                            EventMessageType.ERROR,
                            e.getMessage(),
                            null
                    )
            );
        }
    }
}