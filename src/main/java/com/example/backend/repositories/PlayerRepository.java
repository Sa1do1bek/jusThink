package com.example.backend.repositories;

import com.example.backend.models.Player;
import com.example.backend.responses.PlayerDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> getPlayerById(UUID id);

    List<Player> findAllBySession_Id(UUID sessionId);

    @Query("SELECT new com.example.backend.responses.PlayerDto( " +
            "p.id, " +
            "p.nickName ) " +
            "FROM Player AS p " +
            "WHERE p.session.id = :sessionId")
    List<PlayerDto> getAllBySession_Id(@Param("sessionId") UUID sessionId);



}
