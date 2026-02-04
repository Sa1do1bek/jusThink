package com.example.backend.services.auth.login;

import com.example.backend.enums.Action;
import com.example.backend.enums.ResourceType;
import com.example.backend.enums.Role;
import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.AuditLog;
import com.example.backend.models.UserModel;
import com.example.backend.repositories.AuditLogRepository;
import com.example.backend.requests.LoginRequest;
import com.example.backend.responses.LoginResponse;
import com.example.backend.responses.UserResponse;
import com.example.backend.security.IPAddressService;
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
    private final AuditLogRepository auditLogRepository;
    private final IPAddressService ipAddressService;
    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserModel user = userService.getUserByEmail(request.email());

        if (!user.isEmailVerified())
            throw new IllegalActionException("User has not been verified. Authentication failed!");

        String jwtToken = jwtService.generateToken(auth);
        UserResponse response = userService.converterToUserResponse(user);

        userService.saveUser(user);

        AuditLog log = new AuditLog();
        log.setActor(user);
        log.setActorRole(user != null ? user.getRole() : Role.SYSTEM);
        log.setAction(Action.LOGIN);
        log.setResourceType(ResourceType.USER);
        log.setResourceId(user.getId());
        log.setIpAddress(ipAddressService.getClientIp(httpRequest));
        log.setUserAgent(httpRequest.getHeader("User-Agent"));
        log.setSuccess(true);
        auditLogRepository.save(log);

        return new LoginResponse(jwtToken, response);
    }

}
