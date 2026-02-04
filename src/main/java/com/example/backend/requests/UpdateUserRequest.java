package com.example.backend.requests;

public record UpdateUserRequest(
        String nickName,
        String email,
        String password
) {

}
