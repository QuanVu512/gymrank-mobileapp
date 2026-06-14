package com.gymrank.api.domain;

public record AuthResponse(
        String userId,
        String displayName,
        String email,
        String token,
        boolean newUser
) {
}
