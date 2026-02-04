package com.example.backend.responses;

import java.util.UUID;

public record OptionsForOwners(
        UUID id,
        Integer optionOrder,
        String text,
        Boolean isCorrect
) {

}
