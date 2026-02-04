package com.example.backend.repositories;

import com.example.backend.models.PlayerAnswer;
import com.example.backend.responses.PlayerFinalStats;
import com.example.backend.responses.QuestionWithPlayerAnswerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, UUID> {

    @Query("""
    SELECT new com.example.backend.responses.QuestionWithPlayerAnswerInfo(
        q.id, q.orderNumber, q.text, q.score,
        p.id, p.nickName, st.score,
        o.id, o.text, o.isCorrect
    )
    FROM Session s
    JOIN s.players p
    JOIN p.playerAnswers a
    JOIN a.option o
    JOIN o.question q
    , Statistics st
    WHERE s.id = :sessionId
      AND st.sessionId = s.id
      AND st.playerId = p.id
    ORDER BY q.orderNumber, p.nickName
""")
    List<QuestionWithPlayerAnswerInfo> getQuestionWithPlayerAnswers(@Param("sessionId") UUID sessionId);

    @Query("""
    SELECT new com.example.backend.responses.PlayerFinalStats(
        p.id,
        p.nickName,
        SUM(a.score),
        SUM(CASE WHEN a.score > 0 THEN 1 ELSE 0 END)
    )
    FROM Player p, ActivePlayerAnswer a
    WHERE a.playerId = p.id
      AND a.sessionId = :sessionId
    GROUP BY p.id, p.nickName
""")
    List<PlayerFinalStats> getLeaderboardWithPlayerAnswers(@Param("sessionId") UUID sessionId);

}
