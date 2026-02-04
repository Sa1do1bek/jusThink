package com.example.backend.services.websocket;

import com.example.backend.enums.EventMessageType;
import com.example.backend.enums.SessionStatus;
import com.example.backend.models.*;
import com.example.backend.repositories.SessionRepository;
import com.example.backend.requests.CreatePlayerAnswerRequest;
import com.example.backend.responses.*;
import com.example.backend.services.playerAnswers.PlayerAnswerService;
import com.example.backend.services.question.QuestionService;
import com.example.backend.services.session.ActiveSessionQuestionService;
import com.example.backend.services.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    @Autowired
    @Lazy
    private WebSocketService self;
    private final ActiveSessionQuestionService activeSessionQuestionService;
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerAnswerService playerAnswerService;
    private final SessionService sessionService;
    private final QuestionService questionService;
    private final TaskScheduler scheduler;

    @Transactional
    public void startSession(UUID sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        session.setStatus(SessionStatus.ACTIVE);
        sessionRepository.save(session);

        messageSender(
                sessionId,
                EventMessageType.SESSION_STARTS,
                "Session starts!",
                null
        );

        self.sendNextQuestion(sessionId);
    }

    @Transactional
    public void sendNextQuestion(UUID sessionId) {
        sessionService.lockSessionWithTx(sessionId);

        Instant now = Instant.now();
        List<ActiveSessionQuestion> questions = activeSessionQuestionService.findExpiredQuestions(
                sessionId, now
        );
        questions.forEach(question ->
                    question.setFinished(true)
        );
        activeSessionQuestionService.saveAll(questions);

        ActiveSessionQuestion nextQuestion = activeSessionQuestionService
                .getNextUnaskedQuestion(sessionId);

        if (nextQuestion == null) {
            messageSender(
                    sessionId,
                    EventMessageType.SESSION_ENDS,
                    "Session ends!",
                    playerAnswerService.getFinalLeaderboard(sessionId)
            );
            sessionService.completeSession(sessionId);
            playerAnswerService.savePlayerAnswers(sessionId);
            activeSessionQuestionService.cleanFinishedSession(sessionId);
            return;
        }

        leaderboard(sessionId);

        scheduler.schedule(() -> {
            Instant questionStart = Instant.now();
            Instant expiresAt = questionStart.plusSeconds(nextQuestion.getQuestion().getTimeInSeconds() + 10);

            nextQuestion.setAsked(true);
            nextQuestion.setStartedAt(questionStart);
            nextQuestion.setExpiresAt(expiresAt);
            activeSessionQuestionService.save(nextQuestion);

            QuestionResponse response = questionService.converterToQuestionSafeResponse(nextQuestion.getQuestion());
            messageSender(
                    sessionId,
                    EventMessageType.QUESTION,
                    "Question for session!",
                    response
            );

            scheduler.schedule(() -> {
                nextQuestion.setFinished(true);
                activeSessionQuestionService.save(nextQuestion);

                optionCounts(sessionId, nextQuestion.getQuestion().getId());
                self.sendNextQuestion(sessionId);
            }, Date.from(expiresAt));

        }, Instant.now().plusSeconds(10));
    }

    @Transactional
    public void skipQuestion(UUID sessionId) {
        sessionService.lockSessionWithTx(sessionId);
        ActiveSessionQuestion activeSessionQuestion = activeSessionQuestionService
                .getCurrentActiveQuestion(sessionId);
        if (activeSessionQuestion != null)
            activeSessionQuestionService
                    .finishActiveSessionQuestion(activeSessionQuestion);
        self.sendNextQuestion(sessionId);
    }

    @Transactional
    public void endSession(UUID sessionId) {
        sessionService.lockSessionWithTx(sessionId);
        List<ActiveSessionQuestion> questions = activeSessionQuestionService.getRemainingQuestions(sessionId);
        questions.forEach(question ->
                question.setFinished(true)
        );
        activeSessionQuestionService.saveAll(questions);

        messageSender(
                sessionId,
                EventMessageType.SESSION_ENDS,
                "Session ends!",
                playerAnswerService.getCurrentStats(sessionId)
        );
        sessionService.completeSession(sessionId);
        playerAnswerService.savePlayerAnswers(sessionId);
        activeSessionQuestionService.cleanFinishedSession(sessionId);
    }

    public PlayerAnswerResponse submitAnswer(UUID sessionId, CreatePlayerAnswerRequest request) {
        Instant now = Instant.now();
        ActiveSessionQuestion activeSessionQuestion = activeSessionQuestionService
                .getCurrentActiveQuestion(sessionId);

        if (activeSessionQuestion == null || activeSessionQuestion.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("No active question or question has expired");
        }

        Question question = activeSessionQuestion.getQuestion();
        QuestionOption selectedOption = question.getQuestionOptions()
                .stream()
                .filter(option -> option.getId().equals(request.questionOptionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid option"));

        int timeTakenMilliSeconds = (int) (Duration.between(activeSessionQuestion.getStartedAt(), now)
                .toMillis());
        int score = selectedOption.isCorrect() ?
                question.getScore() + calculateSpeedBonus(
                        timeTakenMilliSeconds / 1000, question.getTimeInSeconds()
                ) : 0;

        playerAnswerService.createPlayerAnswer(sessionId, request, score, timeTakenMilliSeconds);

        return new PlayerAnswerResponse(selectedOption.isCorrect(), score);
    }

    private int calculateSpeedBonus(int timeTakenSeconds, int timeInSeconds) {
        int remainingTime = timeInSeconds - timeTakenSeconds;
        if (remainingTime < 0) remainingTime = 0;
        int bonus = remainingTime * 20;

        int maxBonus = 400;
        return Math.min(bonus, maxBonus);
    }

    private void leaderboard(UUID sessionId) {
        LeaderboardResponse response = playerAnswerService.getCurrentStats(sessionId);
        messageSender(
                sessionId,
                EventMessageType.LEADERBOARD,
                "Current leaderboard!",
                response
        );
    }

    private void optionCounts(UUID sessionId, UUID questionId) {
        List<OptionsCountResponse> response = playerAnswerService.getOptionCounts(sessionId, questionId);
        messageSender(
                sessionId,
                EventMessageType.OPTION_COUNTS,
                "Option counts for the question",
                response
        );
    }

    private void messageSender(
            UUID sessionId, EventMessageType type, String message, Object response
    )  {
        System.out.println("\n\n" + type + " in message sender called\n\n");
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                new EventMessageResponse(type, message, response)
        );
    }
}