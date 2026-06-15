package com.gymrank.api.security;

import com.gymrank.api.persistence.AppUser;
import com.gymrank.api.persistence.AuthSession;
import com.gymrank.api.persistence.AuthSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class TokenService {

    private static final Duration SESSION_TTL = Duration.ofDays(30);

    private final AuthSessionRepository authSessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    @Transactional
    public String createSession(AppUser user) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        authSessionRepository.save(new AuthSession(user, tokenHash, Instant.now().plus(SESSION_TTL)));
        return rawToken;
    }

    @Transactional
    public AppUser requireUser(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw unauthorized();
        }

        Instant now = Instant.now();
        AuthSession session = authSessionRepository.findActiveCandidateByTokenHash(hashToken(rawToken.trim()))
                .filter(candidate -> candidate.isActive(now))
                .orElseThrow(TokenService::unauthorized);
        session.markUsed();
        return session.getUser();
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private static ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ. Hãy đăng nhập lại.");
    }
}
