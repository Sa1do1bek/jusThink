package com.example.backend.services.analytics;

import com.example.backend.enums.Role;
import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.models.UserModel;
import com.example.backend.repositories.PlayerAnswerRepository;
import com.example.backend.repositories.PlayerRepository;
import com.example.backend.repositories.SessionRepository;
import com.example.backend.repositories.StatisticsRepository;
import com.example.backend.responses.PlayersCorrectAnswerPercentage;
import com.example.backend.responses.QuestionAverageTime;
import com.example.backend.responses.QuestionWithPlayerAnswerInfo;
import com.example.backend.services.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PlayerAnswerRepository playerAnswerRepository;
    private final StatisticsRepository statisticsRepository;
    private final SessionService sessionService;

    private void checkUser(UUID sessionId, String email) {
        UserModel user = sessionService.getSessionById(sessionId).getHost();
        if (!user.getEmail().equals(email) && !user.getRole().equals(Role.ADMIN))
            throw new IllegalActionException("Current user cannot access to this action!");
    }

    public Map<UUID, List<QuestionWithPlayerAnswerInfo>> sessionAnalytics(UUID sessionId, String email) {
        checkUser(sessionId, email);
        List<QuestionWithPlayerAnswerInfo> answerInfos = playerAnswerRepository.getQuestionWithPlayerAnswers(sessionId);
        return answerInfos.stream()
                        .collect(Collectors.groupingBy(QuestionWithPlayerAnswerInfo::questionId));
    }

    public List<PlayersCorrectAnswerPercentage> getCorrectnessPercentage(UUID sessionId, String email) {
        checkUser(sessionId, email);
        return statisticsRepository.getAnswerPercentage(sessionId)
                .stream().max(Comparator.comparing(PlayersCorrectAnswerPercentage::correctness))
                .stream().toList();
    }

    public Double getSessionAnswerPercentage(UUID sessionId, String email) {
        checkUser(sessionId, email);
        return statisticsRepository.getSessionAnswerPercentage(sessionId);
    }


    public Integer getPlayersNumberPerSession(UUID sessionId, String email) {
        checkUser(sessionId, email);
        return statisticsRepository.getPlayersNumberPerSession(sessionId);
    }

    public Double getAllAnswerPercentage() {
        return statisticsRepository.getAllAnswerPercentage();
    }

    public Integer getNumberSession() {
        return statisticsRepository.getNumberSession();
    }

    public Double getSessionAnswerAverageTime(UUID sessionId, String email) {
        checkUser(sessionId, email);
        return statisticsRepository.getSessionAnswerAverageTime(sessionId);
    }

    public List<QuestionAverageTime> getQuestionAnswerAverageTime(UUID sessionId, String email) {
        checkUser(sessionId, email);
        return statisticsRepository.getQuestionAnswerAverageTime(sessionId);
    }
}