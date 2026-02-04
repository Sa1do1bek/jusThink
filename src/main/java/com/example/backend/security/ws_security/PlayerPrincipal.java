package com.example.backend.security.ws_security;

import com.example.backend.enums.SessionRole;
import com.example.backend.models.Player;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerPrincipal implements StompPrincipal{

    private final Player player;

    @Override
    public String getRole() {
        return SessionRole.PLAYER.name();
    }

    @Override
    public String getName() {
        return player.getId().toString();
    }
}
