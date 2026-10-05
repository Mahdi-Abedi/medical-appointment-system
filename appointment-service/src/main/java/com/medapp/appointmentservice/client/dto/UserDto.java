package com.medapp.appointmentservice.client.dto;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String role,
        boolean enabled
) {
}