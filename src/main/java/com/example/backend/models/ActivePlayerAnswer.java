package com.example.backend.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "active_player_answers")
public class ActivePlayerAnswer {

    @Id
    @GeneratedValue
    private UUID id;

    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false)
    private UUID questionId;

    @Column(nullable = false)
    private UUID optionId;

    private int score;
}