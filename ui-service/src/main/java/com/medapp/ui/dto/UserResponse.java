package com.medapp.ui.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {}