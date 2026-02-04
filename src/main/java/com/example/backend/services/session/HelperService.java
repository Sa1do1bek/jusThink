package com.example.backend.services.session;

import com.example.backend.enums.EventMessageType;
import com.example.backend.enums.SessionStatus;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Session;
import com.example.backend.repositories.SessionRepository;
import com.example.backend.responses.EventMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HelperService {
    private final SessionRepository sessionRepository;
    private final ActiveSessionQuestionService activeSessionQuestionService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Session getSessionById(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found!"));
    }

    @Transactional
    public void checkSessionStatus(UUID sessionId) {
        System.out.println("\n\n\nchecking 3-minute end\n\n\n");
        Session session = this.getSessionById(sessionId);

        if (session.getStatus() == SessionStatus.WAITED) {
            this.endSession(sessionId);
        }
    }

    @Transactional
    public void lockSessionWithTx(UUID sessionId) {
        sessionRepository.lockSession(sessionId);
    }

    @Transactional
    public void endSession(UUID sessionId) {
        this.lockSessionWithTx(sessionId);
        activeSessionQuestionService.getRemainingQuestions(sessionId)
                .forEach(q -> {
                    q.setFinished(true);
                    activeSessionQuestionService.save(q);
                });

        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                new EventMessageResponse(EventMessageType.SESSION_ENDS, "Session ends!", null)
        );
        this.completeSession(sessionId);
        activeSessionQuestionService.cleanFinishedSession(sessionId);
    }

    @Transactional
    public void completeSession(UUID sessionId) {
        Session session = this.getSessionById(sessionId);
        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);
    }
}
