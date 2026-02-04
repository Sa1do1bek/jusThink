package com.example.backend.repositories;

import com.example.backend.models.ActiveSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActiveSessionQuestionRepository extends JpaRepository<ActiveSessionQuestion, UUID> {
    Optional<ActiveSessionQuestion> findFirstBySession_IdAndAskedFalseOrderByCreatedAtAsc(UUID sessionId);
    Optional<ActiveSessionQuestion> findBySession_IdAndAskedTrueAndFinishedFalse(UUID sessionId);
    Optional<List<ActiveSessionQuestion>> findBySession_IdAndExpiresAt(UUID sessionId, Instant expiresAt);
    Optional<List<ActiveSessionQuestion>> findAllByAskedFalseAndSession_Id(UUID sessionId);
    void removeAllBySession_IdAndFinishedTrue(UUID sessionId);

    @Query("SELECT DISTINCT sq.session.id FROM ActiveSessionQuestion AS sq " +
            "WHERE sq.asked = true AND sq.finished = false")
    List<UUID> findDistinctQuestionIdsBySession();

    @Query("SELECT DISTINCT a.session.id FROM ActiveSessionQuestion a WHERE a.asked = true AND a.finished = false")
    List<UUID> findActiveSessionIds();

}
