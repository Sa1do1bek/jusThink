package com.example.backend.services.authentication.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String verificationLink) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Verify your email");

        String htmlContent = """
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:24px;border-radius:8px;background:#f9f9f9;'>
                <h2 style='color:#2d7ff9;'>Welcome to jasThink!</h2>
                <p style='font-size:16px;color:#333;'>Thank you for registering.<br>
                Please verify your email by clicking the button below:</p>
                <div style='margin:24px 0;'>
                     <a href='http://localhost:3001/verify-email?token=%s' style='display:inline-block;padding:12px 24px;background:#2d7ff9;color:#fff;text-decoration:none;border-radius:4px;font-weight:bold;'>Verify Email</a>
                </div>
                <p style='font-size:14px;color:#888;'>This link expires after an hour.</p>
                <hr style='margin:24px 0;border:none;border-top:1px solid #eee;'>
                <p style='font-size:12px;color:#aaa;'>If you did not request this, please ignore this email.</p>
            </div>
        </body>
        </html>
        """.formatted(verificationLink);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }


}