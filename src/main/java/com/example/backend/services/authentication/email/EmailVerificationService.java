package com.example.backend.services.authentication.email;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.exceptions.TooManyRequestsException;
import com.example.backend.models.EmailVerificationToken;
import com.example.backend.models.UserModel;
import com.example.backend.repositories.EmailVerificationTokenRepository;
import com.example.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    @Value("${email.verification.token.expiration}")
    private int expirationHours;
    @Value("${app.frontend.url}")
    private String frontendUrl;
    @Value("${app.verify-email.endpoint}")
    private String verifyEndpoint;
    @Value("${email.verification.break}")
    private int emailVerificationBreakInMinutes;


    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Transactional
    public void createVerificationToken(UserModel user) throws MessagingException {

        EmailVerificationToken tokenEntity = emailVerificationTokenRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseGet(EmailVerificationToken::new);

        tokenEntity.setUser(user);
        tokenEntity.setToken(UUID.randomUUID().toString());
        tokenEntity.setCreatedAt(LocalDateTime.now());
        tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));

        emailVerificationTokenRepository.save(tokenEntity);

        String verificationLink = frontendUrl + verifyEndpoint + tokenEntity.getToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }

    @Transactional
    public void resendVerificationToken(String email) throws MessagingException {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with " + email + " email not found!"));

        if (user.isEmailVerified())
            throw new IllegalActionException("Email is already verified.");

        EmailVerificationToken lastToken = emailVerificationTokenRepository
                .findTopByUserOrderByCreatedAtDesc(user).orElseGet(null);

        if (lastToken != null) {
            long minutesSinceLast = Duration.between(lastToken.getCreatedAt(), LocalDateTime.now()).toMinutes();
            if (minutesSinceLast < emailVerificationBreakInMinutes)
                throw new TooManyRequestsException(
                        "You can only resend the verification email every " + emailVerificationBreakInMinutes + " minutes!"
                );
            emailVerificationTokenRepository.delete(lastToken);
        }

        createVerificationToken(user);
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalActionException("Invalid verification token"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalActionException("Verification token expired");

        UserModel user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);
    }
}
