package com.example.backend.requests;

import java.util.UUID;

public record UpdateRoleRequest(
    UUID id,
    String name
) {
}
