package com.example.backend.models;

import com.example.backend.enums.Action;
import com.example.backend.enums.ResourceType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
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

    @Column(nullable = false)
    private String actorRole;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel actor;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @PrePersist
    public void prePersist() {
        this.timestamp = Instant.now();
    }

}