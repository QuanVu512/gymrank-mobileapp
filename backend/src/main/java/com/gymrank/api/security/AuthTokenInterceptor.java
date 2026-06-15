package com.gymrank.api.security;

import com.gymrank.api.persistence.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private final TokenService tokenService;

    public AuthTokenInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublicEndpoint(request)) {
            return true;
        }

        String token = extractBearerToken(request.getHeader("Authorization"));
        AppUser user = tokenService.requireUser(token);
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, user);
        return true;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(method)
                || path.equals("/api/v1/health")
                || path.equals("/api/v1/exercises")
                || path.equals("/api/v1/score/preview")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/login");
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw missingToken();
        }

        String prefix = "Bearer ";
        if (!authorizationHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
            throw missingToken();
        }
        return authorizationHeader.substring(prefix.length()).trim();
    }

    private ResponseStatusException missingToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API này cần đăng nhập.");
    }
}
