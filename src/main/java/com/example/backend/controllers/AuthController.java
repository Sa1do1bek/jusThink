package com.example.backend.controllers;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.requests.CreateUserRequest;
import com.example.backend.requests.LoginRequest;
import com.example.backend.responses.ApiResponse;
import com.example.backend.services.auth.login.LoginService;
import com.example.backend.services.auth.email.EmailVerificationService;
import com.example.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final LoginService loginService;
    private final UserService userService;

    @PostMapping(path = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> register(@RequestBody CreateUserRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "User created successfully, but not verified yet!",
                    userService.converterToUserResponse(userService.createUser(request)))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        try {
            emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok(new ApiResponse("User verified successfully!", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Login successfully!", loginService.login(request, httpRequest))

            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid email or password", null));
        } catch (IllegalActionException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("User has not been verified. Authentication failed!", null));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Authentication failed", null));
        }
    }
}