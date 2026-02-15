package com.example.backend.repositories;

import com.example.backend.models.EmailVerificationToken;
import com.example.backend.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteAllByExpiresAtBefore(LocalDateTime now);
    void deleteAllByUser(UserModel user);
    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(UserModel user);
}
