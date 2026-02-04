package com.example.backend.responses;

import com.example.backend.enums.SessionRole;

import java.util.UUID;

public record UserInfoWS(
        SessionRole role,
        UUID userId
) {
}
