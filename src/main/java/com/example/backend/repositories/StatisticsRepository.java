    package com.example.backend.repositories;

import com.example.backend.models.Statistics;
import com.example.backend.responses.PlayersCorrectAnswerPercentage;
import com.example.backend.responses.QuestionAverageTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StatisticsRepository extends JpaRepository<Statistics, UUID> {
    @Query("""
        SELECT new com.example.backend.responses.PlayersCorrectAnswerPercentage(
        p.nickName,
        (COUNT(s1) * 100.0) /
        (SELECT COUNT(DISTINCT s2.questionId)
        FROM Statistics s2
        WHERE s2.sessionId = :sessionId)
        )
        FROM Statistics s1
        JOIN Player p ON p.id = s1.playerId
        WHERE s1.sessionId = :sessionId AND s1.isCorrect = true
        GROUP BY p.id, p.nickName
    """)
    List<PlayersCorrectAnswerPercentage> getAnswerPercentage(@Param("sessionId") UUID sessionId);

    @Query("""
        SELECT (COUNT(s1.questionId) * 100.0) /
        (SELECT COUNT(s2.questionId)
        FROM Statistics s2
        WHERE s2.sessionId = :sessionId)
        FROM Statistics s1
        WHERE s1.isCorrect = true AND s1.sessionId = :sessionId
    """)
    Double getSessionAnswerPercentage(@Param("sessionId") UUID sessionId);


    @Query("""
    SELECT COALESCE(
        (SUM(CASE WHEN s.isCorrect = true THEN 1 ELSE 0 END) * 100.0) / COUNT(s),
        0.0
    )
    FROM Statistics s
    """)
    Double getAllAnswerPercentage();


    @Query("""
    SELECT (COUNT(DISTINCT s.playerId))
    FROM Statistics  s
    WHERE s.sessionId = :sessionId
    """)
    Integer getPlayersNumberPerSession(@Param("sessionId") UUID sessionId);

    @Query("""
    SELECT AVG(s.timeAnswered)
    FROM Statistics s
    WHERE s.sessionId = :sessionId
    """)
    Double getSessionAnswerAverageTime(@Param("sessionId") UUID sessionId);

    @Query("""
        SELECT new com.example.backend.responses.QuestionAverageTime(
            q.orderNumber, q.text, q.timeInSeconds, AVG(s.timeAnswered)
        )
        FROM Statistics s
        JOIN Question q
            ON q.id = s.questionId
        WHERE s.sessionId = :sessionId
        GROUP BY q.id, q.orderNumber, q.text, q.timeInSeconds
        ORDER BY q.orderNumber
    """)
        List<QuestionAverageTime> getQuestionAnswerAverageTime(@Param("sessionId") UUID sessionId);

    @Query("""
        SELECT (COUNT(s.id))
        FROM Session s
        WHERE s.status = 'COMPLETED'
    """)
        Integer getNumberSession();
}