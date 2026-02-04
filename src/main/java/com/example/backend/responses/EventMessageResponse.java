package com.example.backend.responses;

import com.example.backend.enums.EventMessageType;

public record EventMessageResponse(
        EventMessageType messageType,
        String message,
        Object data
) {
}
