package com.example.backend.responses;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        String nickName,
        LocalDate createdAt,
        List<PlayerAnswerResponse> answers
) {
}
