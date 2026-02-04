package com.example.backend.repositories;

import com.example.backend.models.Session;
import com.example.backend.responses.PlayerFinalStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Session findBySessionCode(String sessionCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Session s WHERE s.id = :sessionId")
    Session lockSession(@Param("sessionId") UUID sessionId);

    @Query(value = "SELECT COUNT(q.id) " +
            "FROM sessions s " +
            "JOIN quiz_versions v ON s.quiz_version_id = v.id " +
            "JOIN questions q ON v.id = q.quiz_version_id " +
            "WHERE s.id = :sessionId ", nativeQuery = true)
    Long getQuestionSize(@Param("sessionId") UUID sessionId);

    Optional<Session> findByConnectionId(String connectionId);
}
