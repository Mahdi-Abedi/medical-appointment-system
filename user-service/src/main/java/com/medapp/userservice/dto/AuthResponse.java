package com.medapp.userservice.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {}