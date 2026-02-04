package com.example.backend.security.ws_security;

import java.security.Principal;

public interface StompPrincipal extends Principal {
    String getRole();
}
