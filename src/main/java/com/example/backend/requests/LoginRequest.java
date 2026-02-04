package com.example.backend.requests;

public record LoginRequest(
        String email,
        String password
) {

}
