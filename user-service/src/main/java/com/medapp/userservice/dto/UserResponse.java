package com.medapp.userservice.dto;

import com.medapp.userservice.entity.Role;
import com.medapp.userservice.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Role role,
        boolean enabled,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}