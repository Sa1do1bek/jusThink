package com.example.backend.requests;

import com.example.backend.enums.Role;

public record CreateUserRequest(
        String nickName,
        String email,
        String password,
        Role role
) {

}
