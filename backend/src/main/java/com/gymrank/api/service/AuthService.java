package com.gymrank.api.service;

import com.gymrank.api.domain.AuthRequest;
import com.gymrank.api.domain.AuthResponse;
import com.gymrank.api.persistence.AppUser;
import com.gymrank.api.persistence.AppUserRepository;
import com.gymrank.api.persistence.UserStats;
import com.gymrank.api.persistence.UserStatsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final UserStatsRepository userStatsRepository;

    public AuthService(AppUserRepository appUserRepository, UserStatsRepository userStatsRepository) {
        this.appUserRepository = appUserRepository;
        this.userStatsRepository = userStatsRepository;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        String email = normalizeEmail(request.email());
        String displayName = cleanDisplayName(request.displayName(), email);

        if (appUserRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email da duoc dang ky.");
        }

        AppUser user = appUserRepository.save(new AppUser(email, createPasswordHash(request.password()), displayName));
        userStatsRepository.save(new UserStats(user));

        return toResponse(user, true);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String email = normalizeEmail(request.email());
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoac mat khau khong dung."));

        if (!isPasswordValid(user.getPasswordHash(), request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoac mat khau khong dung.");
        }

        user.markLoggedIn();
        return toResponse(user, false);
    }

    private AuthResponse toResponse(AppUser user, boolean newUser) {
        return new AuthResponse(
                user.getPublicId().toString(),
                user.getFullName(),
                user.getEmail(),
                UUID.randomUUID().toString(),
                newUser
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanDisplayName(String displayName, String email) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return email.substring(0, email.indexOf('@'));
        }
        return displayName.trim();
    }

    private String createPasswordHash(String password) {
        String salt = UUID.randomUUID().toString();
        return salt + ":" + hashPassword(salt, password);
    }

    private boolean isPasswordValid(String storedPasswordHash, String password) {
        if (storedPasswordHash == null || !storedPasswordHash.contains(":")) {
            return false;
        }

        String[] parts = storedPasswordHash.split(":", 2);
        String salt = parts[0];
        String expectedHash = parts[1];
        return expectedHash.equals(hashPassword(salt, password));
    }

    private String hashPassword(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
