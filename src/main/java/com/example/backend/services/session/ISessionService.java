package com.example.backend.services.session;

import com.example.backend.models.Session;
import com.example.backend.requests.CreateSessionRequest;
import com.example.backend.responses.SessionResponse;

import java.util.List;
import java.util.UUID;

public interface ISessionService {
    Session getSessionById(UUID id);
    List<Session> getAllSessions();
    Session getSessionBySessionCode(String sessionCode);
    Session createSession(CreateSessionRequest sessionRequest, String userEmail);
    SessionResponse converterToSessionResponse(Session session);
    String generateSessionCode();
}
