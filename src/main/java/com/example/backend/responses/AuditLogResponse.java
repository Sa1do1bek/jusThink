package com.example.backend.responses;

import com.example.backend.enums.Action;
import com.example.backend.enums.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID resourceId,
        String userAgent,
        boolean success,
        Instant timestamp,
        ResourceType resourceType,
        Action action,
        UserResponse actor,
        String actorRole,
        String metaData
) {
}
