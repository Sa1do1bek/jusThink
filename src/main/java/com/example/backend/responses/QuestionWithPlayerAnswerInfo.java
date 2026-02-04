package com.example.backend.responses;

import java.util.UUID;

public record QuestionWithPlayerAnswerInfo(
        UUID questionId,
        Integer orderNumber,
        String questionText,
        Integer questionScore,
        UUID playerId,
        String playerNickName,
        Integer playerScore,
        UUID optionId,
        String optionText,
        Boolean optionIsCorrect
) {}