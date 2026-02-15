package com.example.backend.responses;

import com.example.backend.models.Image;
import com.example.backend.models.Role;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nickName,
        String email,
        Role role,
        Image image,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}