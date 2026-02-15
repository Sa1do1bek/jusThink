package com.example.backend.requests;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreateRoleRequest(
        String name,
        Set<UUID> permissionIds
) {
}
