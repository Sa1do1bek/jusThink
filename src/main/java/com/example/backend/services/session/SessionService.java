package com.example.backend.services.session;

import com.example.backend.enums.SessionMode;
import com.example.backend.enums.SessionStatus;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.*;
import com.example.backend.repositories.*;
import com.example.backend.requests.CreateSessionRequest;
import com.example.backend.responses.*;
import com.example.backend.services.question.QuestionService;
import com.example.backend.services.user.UserService;
import com.example.backend.services.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class SessionService implements ISessionService {

    private final ActiveSessionQuestionService activeSessionQuestionService;
    private final SessionRepository sessionRepository;
    private final QuizVersionRepository quizVersionRepository;
    private final UserService userService;
    private final QuestionService questionService;
    private final TaskScheduler taskScheduler;
    private final HelperService helperService;

    @Transactional
    @Override
    public Session getSessionById(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found!"));
    }

    @Transactional
    public void lockSessionWithTx(UUID sessionId) {
        sessionRepository.lockSession(sessionId);
    }

    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Session getSessionBySessionCode(String sessionCode) {
        return sessionRepository.findBySessionCode(sessionCode);
    }

    @Override
    @Transactional
    public Session createSession(CreateSessionRequest sessionRequest, String userEmail) {
        UserModel user = userService.getUserByEmail(userEmail);

        Optional<QuizVersion> latestVersion = quizVersionRepository.findByQuizId(sessionRequest.quizId())
                .stream()
                .max(Comparator.comparing(QuizVersion::getVersionId));

        Session session = new Session();
        session.setHost(user);
        session.setSessionCode(generateSessionCode());
        session.setStatus(sessionRequest.status());
        latestVersion.ifPresent(session::setQuizVersion);
        session = sessionRepository.save(session);

        Session finalSession = session;
        latestVersion.get().getQuestions()
                .forEach(q -> activeSessionQuestionService.createActiveSessionQuestion(finalSession, q.getId()));

        Instant runAt = Instant.now().plus(3, ChronoUnit.MINUTES);

        taskScheduler.schedule(() -> helperService.checkSessionStatus(finalSession.getId()), Date.from(runAt));
        return sessionRepository.save(session);
    }


    @Override
    public String generateSessionCode() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
    }

    @Override
    public SessionResponse converterToSessionResponse(Session session) {
        List<PlayerResponse> responseList = session.getPlayers() != null ?
                session.getPlayers()
                        .stream()
                        .map(questionService::converterToPlayerResponse)
                        .toList() : null;

        return new SessionResponse(
                session.getId(),
                session.getSessionCode(),
                session.getStatus(),
                session.getMode(),
                userService.converterToUserResponse(session.getHost()),
                converterToQuizVersionResponse(session.getQuizVersion()),
                responseList,
                session.getStartedAt(),
                session.getEndedAt()
        );
    }

    public QuizVersionResponse converterToQuizVersionResponse(QuizVersion quizVersion) {
        List<QuestionForOwners> responseList = quizVersion.getQuestions()
                .stream()
                .map(questionService::converterToQuestionResponse)
                .toList();

        return new QuizVersionResponse(
                quizVersion.getId(),
                quizVersion.getVersionId(),
                quizVersion.getPublishedAt(),
                responseList,
                quizVersion.getQuiz().getId()
        );
    }

//    public QuizResponse converterToQuizResponse(Quiz quiz) {
//        Optional<QuizVersion> latestVersion = quiz.getQuizVersions()
//                .stream()
//                .max(Comparator.comparing(QuizVersion::getVersionId));
//        return new QuizResponse(
//                quiz.getId(),
//                quiz.getTitle(),
//                quiz.getDescription(),
//                userService.converterToUserResponse(quiz.getOwner()),
//                quiz.getCreatedAt(),
//                latestVersion
//                        .map(this::converterToQuizVersionResponse)
//                        .orElse(null)
//        );
//    }

    @Transactional
    public void completeSession(UUID sessionId) {
        Session session = this.getSessionById(sessionId);
        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);
    }

    @Transactional
    public void saveStompSessionId(UUID sessionId, String stompSessionId) {
        Session session = this.getSessionById(sessionId);
        session.setConnectionId(stompSessionId);

        sessionRepository.save(session);
    }

    @Transactional
    public Session removeStompSessionId(String stompSessionId) {
        Session session = sessionRepository.findByConnectionId(stompSessionId)
                .orElse(null);
        if (session == null) return null;
        session.setConnectionId(SessionStatus.COMPLETED.name());

        return sessionRepository.save(session);
    }

}