package com.example.backend.services.session;

import com.example.backend.models.ActiveSessionQuestion;
import com.example.backend.models.Session;
import com.example.backend.repositories.ActiveSessionQuestionRepository;
import com.example.backend.services.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActiveSessionQuestionService implements IActiveSessionQuestionService{

    private final ActiveSessionQuestionRepository repository;
    private final QuestionService questionService;

    @Transactional
    @Override
    public ActiveSessionQuestion createActiveSessionQuestion(Session session, UUID questionId) {
        ActiveSessionQuestion activeSessionQuestion = new ActiveSessionQuestion();
        activeSessionQuestion.setSession(session);
        activeSessionQuestion.setQuestion(questionService.getQuestionById(questionId));
        return repository.save(activeSessionQuestion);
    }

    @Override
    public ActiveSessionQuestion getNextUnaskedQuestion(UUID sessionId) {
        return repository.findFirstBySession_IdAndAskedFalseOrderByCreatedAtAsc(sessionId)
                .orElse(null);
    }

    @Transactional
    @Override
    public ActiveSessionQuestion updateActiveSessionQuestion(ActiveSessionQuestion activeSessionQuestion, int timeForExpiry) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(timeForExpiry);

        activeSessionQuestion.setAsked(true);
        activeSessionQuestion.setStartedAt(now);
        activeSessionQuestion.setExpiresAt(expiresAt);
        return repository.save(activeSessionQuestion);
    }

    @Override
    public ActiveSessionQuestion getCurrentActiveQuestion(UUID sessionId) {
        return repository.findBySession_IdAndAskedTrueAndFinishedFalse(sessionId)
                .orElse(null);
    }

    @Override
    public List<ActiveSessionQuestion> getRemainingQuestions(UUID sessionId) {
        return repository.findAllByAskedFalseAndSession_Id(sessionId)
                .orElse(null);
    }

    @Override
    public List<ActiveSessionQuestion> findExpiredQuestions(UUID sessionId, Instant now) {
        return repository.findBySession_IdAndExpiresAt(sessionId, now)
                .orElse(null);
    }

    @Transactional
    @Override
    public void finishActiveSessionQuestion(ActiveSessionQuestion activeSessionQuestion) {
        activeSessionQuestion.setFinished(true);
        repository.save(activeSessionQuestion);
    }

    @Override
    public List<UUID> getActiveSessionIds() {
        return repository.findActiveSessionIds();
    }

    @Transactional
    @Override
    public void save(ActiveSessionQuestion activeSessionQuestion) {
        repository.save(activeSessionQuestion);
    }

    @Transactional
    @Override
    public void saveAll(List<ActiveSessionQuestion> activeSessionQuestions) {
        repository.saveAll(activeSessionQuestions);
    }

    @Transactional
    @Override
    public void cleanFinishedSession(UUID sessionId) {
        repository.removeAllBySession_IdAndFinishedTrue(sessionId);
    }

}