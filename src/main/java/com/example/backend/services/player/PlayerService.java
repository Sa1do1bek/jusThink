package com.example.backend.services.player;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Player;
import com.example.backend.models.Session;
import com.example.backend.repositories.PlayerRepository;
import com.example.backend.repositories.SessionRepository;
import com.example.backend.requests.CreatePlayerRequest;
import com.example.backend.responses.PlayerAnswerResponse;
import com.example.backend.responses.PlayerDto;
import com.example.backend.responses.PlayerResponse;
import com.example.backend.services.question.QuestionService;
import com.example.backend.services.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService implements IPlayerService{

    private final SessionService sessionService;
    private final PlayerRepository playerRepository;
    private final QuestionService questionService;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public Player createPlayer(CreatePlayerRequest request) {
        Session session = sessionService.getSessionBySessionCode(request.sessionCode());
        if (session == null)
            throw new ResourceNotFoundException("Session not found");

        boolean exists = session.getPlayers().stream()
                .anyMatch(p -> p.getNickName().equalsIgnoreCase(request.nickName()));

        if (exists)
            throw new IllegalActionException("Nickname already taken!");

        Player player = new Player();
        player.setNickName(request.nickName());
        player.setSession(session);

        session.getPlayers().add(player);

        return playerRepository.save(player);
    }

    @Override
    public void deletePlayer(UUID playerId) {
        playerRepository.deleteById(playerId);
    }

    public PlayerDto converterToPlayerDto(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getNickName()
        );
    }

    @Override
    public PlayerResponse converterToPlayerResponse(Player player) {
        List<PlayerAnswerResponse> responseList = player.getPlayerAnswers() != null ?
                player.getPlayerAnswers()
                        .stream()
                        .map(questionService::converterToPlayerAnswerResponse)
                        .toList() : null;
        return new PlayerResponse(
                player.getId(),
                player.getNickName(),
                player.getCreatedAt(),
                responseList
        );
    }

    @Override
    public Player getPlayerById(UUID playerId) {
        return playerRepository.getPlayerById(playerId)
                .orElse(null);
    }

    public List<Player> getAllBySessionId(UUID sessionId) {
        return playerRepository.findAllBySession_Id(sessionId);
    }
}
