package com.example.backend.requests;

import com.example.backend.models.Role;

public record CreateUserRequest(
        String nickName,
        String email,
        String password,
        Role role
) {

}
