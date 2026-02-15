package com.example.backend.responses;

import java.time.LocalDate;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String name,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}
