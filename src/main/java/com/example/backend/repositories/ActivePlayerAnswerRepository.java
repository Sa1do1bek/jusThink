package com.example.backend.repositories;

import com.example.backend.models.ActivePlayerAnswer;
import com.example.backend.responses.AnswerDto;
import com.example.backend.responses.OptionsCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActivePlayerAnswerRepository extends JpaRepository<ActivePlayerAnswer, UUID> {
    boolean existsBySessionIdAndQuestionIdAndPlayerId(UUID sessionId, UUID questionId, UUID playerId);
    List<ActivePlayerAnswer> getAllBySessionId(UUID sessionId);
    void deleteAllBySessionId(UUID sessionId);

    @Query("SELECT new com.example.backend.responses.AnswerDto(a.playerId, SUM(a.score)) " +
            "FROM ActivePlayerAnswer a " +
            "WHERE a.sessionId = :sessionId " +
            "GROUP BY a.playerId")
    List<AnswerDto> getPlayerScoresBySession(@Param("sessionId") UUID sessionId);

    @Query("""
        SELECT new com.example.backend.responses.OptionsCountResponse(
            o.optionOrder,
            COUNT(a),
            o.isCorrect
        )
        FROM ActivePlayerAnswer a
        JOIN QuestionOption o
            ON a.optionId = o.id
        WHERE a.sessionId = :sessionId AND a.questionId = :questionId
        GROUP BY o.optionOrder, o.isCorrect
        ORDER BY o.optionOrder
""")
    List<OptionsCountResponse> getOptionCounts(
            @Param("sessionId") UUID sessionId, @Param("questionId") UUID questionId
    );
}
