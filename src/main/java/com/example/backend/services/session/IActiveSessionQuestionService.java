package com.example.backend.services.session;

import com.example.backend.models.ActiveSessionQuestion;
import com.example.backend.models.Session;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IActiveSessionQuestionService {
    ActiveSessionQuestion createActiveSessionQuestion(Session session, UUID questionId);
    ActiveSessionQuestion getNextUnaskedQuestion(UUID sessionId);
    ActiveSessionQuestion updateActiveSessionQuestion(ActiveSessionQuestion activeSessionQuestion, int timeForExpiry);
    ActiveSessionQuestion getCurrentActiveQuestion(UUID sessionId);
    List<ActiveSessionQuestion> getRemainingQuestions(UUID sessionId);
    List<ActiveSessionQuestion> findExpiredQuestions(UUID sessionId, Instant now);
    void finishActiveSessionQuestion(ActiveSessionQuestion activeSessionQuestion);
    List<UUID> getActiveSessionIds();
    void save(ActiveSessionQuestion activeSessionQuestion);

    @Transactional
    void saveAll(List<ActiveSessionQuestion> activeSessionQuestions);

    void cleanFinishedSession(UUID sessionId);
}
