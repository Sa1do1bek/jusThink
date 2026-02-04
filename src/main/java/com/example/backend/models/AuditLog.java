package com.example.backend.models;

import com.example.backend.enums.Action;
import com.example.backend.enums.ResourceType;
import com.example.backend.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_resource", columnList = "resourceType, resourceId")
        }
)
public class AuditLog {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID resourceId;
    private String ipAddress;
    private String userAgent;
    private boolean success;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role actorRole;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel actor;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @PrePersist
    public void prePersist() {
        this.timestamp = Instant.now();
    }

}