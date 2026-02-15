package com.example.backend.requests;

import java.util.UUID;

public record UpdatePermissionRequest(
        UUID id,
        String name
) {
}
