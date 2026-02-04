package com.example.backend.security.ws_security;

import com.example.backend.enums.SessionRole;
import com.example.backend.models.UserModel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HostPrincipal implements StompPrincipal{

    private final UserModel user;

    @Override
    public String getRole() {
        return SessionRole.HOST.name();
    }

    @Override
    public String getName() {
        return user.getId().toString();
    }
}
