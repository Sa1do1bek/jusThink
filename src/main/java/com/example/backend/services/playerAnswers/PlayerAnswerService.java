package com.example.backend.services.playerAnswers;

import com.example.backend.exceptions.AlreadyExistsException;
import com.example.backend.models.*;
import com.example.backend.repositories.*;
import com.example.backend.requests.CreatePlayerAnswerRequest;
import com.example.backend.responses.*;
import com.example.backend.services.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerAnswerService implements IPlayerAnswerService{

    private final PlayerAnswerRepository repository;
    private final ActivePlayerAnswerRepository activePlayerAnswerRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final StatisticsRepository statisticsRepository;
    private final SessionRepository sessionRepository;
    private final PlayerRepository playerRepository;
    private final QuestionService questionService;

    @Override
    @Transactional
    public void createPlayerAnswer(UUID sessionId, CreatePlayerAnswerRequest request, int score, int timeTakenSeconds) {
        System.out.println("\n\n\n\n" +
                "\n Session:" + sessionId + "\nQuestion: " + request.questionId()
                + "\nPlayer: " + request.playerId() + "\n\n"
        );
        if (activePlayerAnswerRepository.existsBySessionIdAndQuestionIdAndPlayerId(
                sessionId, request.questionId(), request.playerId()
        )) {
            throw new AlreadyExistsException("Player already answered this question");
        }

        ActivePlayerAnswer answer = new ActivePlayerAnswer();
        answer.setSessionId(sessionId);
        answer.setPlayerId(request.playerId());
        answer.setQuestionId(request.questionId());
        answer.setOptionId(request.questionOptionId());
        answer.setScore(score);

        Statistics statistics = new Statistics();
        statistics.setSessionId(answer.getSessionId());
        statistics.setQuestionId(answer.getQuestionId());
        statistics.setPlayerId(answer.getPlayerId());
        statistics.setCorrect(score != 0);
        statistics.setScore(answer.getScore());
        statistics.setOptionId(answer.getOptionId());
        statistics.setTimeAnswered((float) timeTakenSeconds / 1000);

        statisticsRepository.save(statistics);
        activePlayerAnswerRepository.save(answer);
    }

    @Transactional
    public void savePlayerAnswers(UUID sessionId) {
        List<ActivePlayerAnswer> answers = activePlayerAnswerRepository.getAllBySessionId(sessionId);

        repository.saveAll(answers.stream().map(answer -> {
            PlayerAnswer playerAnswer = new PlayerAnswer();
            playerAnswer.setPlayer(playerRepository.getReferenceById(answer.getPlayerId()));
            playerAnswer.setOption(questionOptionRepository.getReferenceById(answer.getOptionId()));
            return playerAnswer;
        }).toList());
        activePlayerAnswerRepository.deleteAllBySessionId(sessionId);
    }

    public LeaderboardResponse getCurrentStats(UUID sessionId) {
        List<AnswerDto> answers = activePlayerAnswerRepository.getPlayerScoresBySession(sessionId);
        List<PlayerDto> players = playerRepository.getAllBySession_Id(sessionId);

        Map<UUID, PlayerDto> playerMap = players.stream()
                .collect(Collectors.toMap(PlayerDto::playerId, p -> p));

        List<PlayerStatsResponse> stats = answers.stream()
                .map(a -> {
                    PlayerDto player = playerMap.get(a.playerId());
                    return player == null ? null :
                            new PlayerStatsResponse(player.nickName(), a.score());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(PlayerStatsResponse::score).reversed())
                .toList();

        return new LeaderboardResponse(stats);
    }

    public FinalLeaderboard getFinalLeaderboard(UUID sessionId) {
        List<PlayerFinalStats> playerFinalStats = repository.getLeaderboardWithPlayerAnswers(sessionId);
        Long totalQuestions = sessionRepository.getQuestionSize(sessionId);

        return new FinalLeaderboard(totalQuestions, playerFinalStats);
    }

    public List<OptionsCountResponse> getOptionCounts(UUID sessionId, UUID questionId) {
        Set<QuestionOption> options = questionService.getQuestionById(questionId).getQuestionOptions();
        List<OptionsCountResponse> countsFromDb = activePlayerAnswerRepository.getOptionCounts(sessionId, questionId);
        Map<Integer, OptionsCountResponse> countsMap = countsFromDb.stream()
                .collect(Collectors.toMap(
                        OptionsCountResponse::optionOrder,
                        Function.identity()
                ));
        return options.stream()
                .map(option -> countsMap.getOrDefault(
                        option.getOptionOrder(),
                        new OptionsCountResponse(option.getOptionOrder(), 0L, option.isCorrect())
                ))
                .sorted(Comparator.comparingInt(OptionsCountResponse::optionOrder))
                .toList();
    }
}