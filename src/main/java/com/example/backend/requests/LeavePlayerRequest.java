package com.example.backend.requests;

import java.util.UUID;

public record LeavePlayerRequest(
        UUID sessionId,
        UUID playerId
) {
}
