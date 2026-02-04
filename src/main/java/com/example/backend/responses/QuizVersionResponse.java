package com.example.backend.responses;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuizVersionResponse(
        UUID id,
        int versionId,
        LocalDate publishedAt,
        List<QuestionForOwners> questionResponses,
        UUID quizId
) {
}
