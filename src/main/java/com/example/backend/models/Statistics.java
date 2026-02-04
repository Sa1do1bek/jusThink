package com.example.backend.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "statistics")
public class Statistics {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID sessionId;
    private UUID questionId;
    private UUID optionId;
    private UUID playerId;
    private boolean isCorrect;
    private int score;
    private float timeAnswered;
}
