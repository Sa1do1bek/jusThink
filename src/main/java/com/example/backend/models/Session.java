package com.example.backend.models;

import com.example.backend.enums.SessionMode;
import com.example.backend.enums.SessionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue
    private UUID id;
    private String sessionCode;
    private LocalDate startedAt;
    private LocalDate endedAt;

    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    @ManyToOne
    @JoinColumn(name = "quiz_version_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuizVersion quizVersion;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private UserModel host;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActiveSessionQuestion> activeSessions;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Player> players = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.WAITED;

    @Enumerated(EnumType.STRING)
    private SessionMode mode = SessionMode.LIVE;

    private String connectionId;
}