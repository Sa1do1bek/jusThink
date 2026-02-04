package com.example.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class IPAddressService {

    public String getClientIp(HttpServletRequest request) {
        String flyIp = request.getHeader("Fly-Client-IP");
        if (flyIp != null && !flyIp.isBlank())
            return flyIp;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor))
            return xForwardedFor.split(",")[0].trim();

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp))
            return xRealIp;

        return request.getRemoteAddr();
    }
}
