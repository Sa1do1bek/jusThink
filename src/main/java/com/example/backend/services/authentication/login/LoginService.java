package com.example.backend.services.authentication.login;

import com.example.backend.enums.Action;
import com.example.backend.enums.ResourceType;
import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.models.UserModel;
import com.example.backend.requests.LoginRequest;
import com.example.backend.responses.LoginResponse;
import com.example.backend.responses.UserResponse;
import com.example.backend.services.auditLogging.AuditLoggingService;
import com.example.backend.services.jwt.JwtService;
import com.example.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class LoginService implements ILoginService{

    private final AuthenticationManager authenticationManager;
    private final AuditLoggingService auditLoggingService;
    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            UserModel user = userService.getUserByEmail(request.email());

            if (!user.isEmailVerified()) {
                auditLoggingService.createAuditLog(
                        user, Action.LOGIN, ResourceType.USER, user.getId(),
                        httpRequest, false, "{\"reason\":\"EMAIL_NOT_VERIFIED\"}"
                );
                throw new IllegalActionException("User has not been verified. Authentication failed!");
            }

            String jwtToken = jwtService.generateToken(auth);
            UserResponse response = userService.converterToUserResponse(user);

            userService.saveUser(user);

            auditLoggingService.createAuditLog(
                    user, Action.LOGIN, ResourceType.USER, user.getId(),
                    httpRequest, true, null
            );

            return new LoginResponse(jwtToken, response);

        } catch (Exception ex) {
            auditLoggingService.createAuditLog(
                    null, Action.LOGIN, ResourceType.USER, null,
                    httpRequest, false, "{\"reason\":\"BAD_CREDENTIALS\"}"
            );
            throw ex;
        }
    }


}
