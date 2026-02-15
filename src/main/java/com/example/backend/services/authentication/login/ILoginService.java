package com.example.backend.services.authentication.login;

import com.example.backend.requests.LoginRequest;
import com.example.backend.responses.LoginResponse;

import javax.servlet.http.HttpServletRequest;

public interface ILoginService {
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
}
