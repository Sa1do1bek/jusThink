package com.example.backend.responses;

import java.util.UUID;

public record LoginResponse(
        String token,
        UserResponse user
) {

}
