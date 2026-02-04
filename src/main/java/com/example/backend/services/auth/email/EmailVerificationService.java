package com.example.backend.services.auth.email;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.models.EmailVerificationToken;
import com.example.backend.models.UserModel;
import com.example.backend.repositories.EmailVerificationTokenRepository;
import com.example.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    @Value("${email.verification.token.expiration}")
    private int expiration;
    @Value("${app.frontend.url}")
    private String link;
    @Value("${app.verify-email.endpoint}")
    private String endpoint;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public void createVerificationToken(UserModel user) {
        emailVerificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(
                LocalDateTime.now().plusHours(expiration)
        );

        emailVerificationTokenRepository.save(verificationToken);

        String verificationLink =
                link + endpoint + token;

        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationLink
        );
    }

    public void verifyEmail(String token) {

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new IllegalActionException("Invalid verification token"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalActionException("Verification token expired");

        UserModel user = verificationToken.getUser();
        user.setEmailVerified(true);

        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);
    }
}
