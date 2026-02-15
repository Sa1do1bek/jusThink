package com.example.backend.responses;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        LocalDate createdAt,
        LocalDate updatedAt,
        List<PermissionResponse> permissions
) {
}
