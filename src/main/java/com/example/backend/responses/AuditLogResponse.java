package com.example.backend.responses;

import java.sql.Timestamp;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String action,
        String entityType,
        UUID entityId,
        Timestamp timestamp,
        UserResponse user
) {
}
