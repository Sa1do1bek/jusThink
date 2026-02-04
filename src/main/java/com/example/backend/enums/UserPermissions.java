package com.example.backend.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserPermissions {
    QUIZ_CREATE("quiz:create"),
    QUIZ_GET("quiz:get"),
    SESSION_CREATE("session:create"),
    SESSION_GET("session:get");

    private final String permission;

    public String getPermission() {
        return permission;
    }
}
