package com.medapp.ui.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String phone
) {}