package com.example.backend.services.jwt;

import com.example.backend.enums.SessionRole;
import com.example.backend.responses.UserInfoWS;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private String expiration;

    public String generateToken(Authentication auth) {
        return Jwts.builder()
                .setSubject(auth.getName())
                .claim("roles", auth.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(expiration)))
                .signWith(
                        Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))
                )
                .compact();
    }
    public String generateWSToken(UUID userId, SessionRole role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(expiration)))
                .signWith(
                        Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))
                )
                .compact();
    }

    public UserInfoWS validateAndGetUserInfo(String token) {

        if (token == null || token.isBlank()) return null;
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            UUID userId = UUID.fromString(claims.getSubject());

            String roleStr = claims.get("type", String.class);


            SessionRole role;
            try {
                role = SessionRole.valueOf(roleStr);
            } catch (Exception e) {
                throw new IllegalStateException("Invalid WS role in token");
            }

            return new UserInfoWS(role, userId);

        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalStateException(String.format("Token %s cannot be trusted", token));
        }
    }
}
