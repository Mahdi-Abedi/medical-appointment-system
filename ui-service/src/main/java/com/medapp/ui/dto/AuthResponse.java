package com.medapp.ui.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {}