package com.example.backend.responses;

import java.time.LocalDate;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}
