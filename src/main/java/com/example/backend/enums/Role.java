package com.example.backend.enums;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Role {
    USER(Sets.newHashSet(UserPermissions.SESSION_GET,
            UserPermissions.QUIZ_GET)),
    CREATOR(Sets.newHashSet(UserPermissions.SESSION_GET,
            UserPermissions.QUIZ_GET,
            UserPermissions.SESSION_CREATE,
            UserPermissions.QUIZ_CREATE)),
    ADMIN(Sets.newHashSet(UserPermissions.SESSION_GET,
            UserPermissions.QUIZ_GET,
            UserPermissions.SESSION_CREATE,
            UserPermissions.QUIZ_CREATE)),
    SYSTEM(Sets.newHashSet(UserPermissions.SESSION_GET,
            UserPermissions.QUIZ_GET,
            UserPermissions.SESSION_CREATE,
            UserPermissions.QUIZ_CREATE));

    private final Set<UserPermissions> permissions;

    public Set<UserPermissions> getPermissions() {
        return permissions;
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return permissions;
    }
}