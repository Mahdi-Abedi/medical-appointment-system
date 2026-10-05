package com.medapp.ui.dto;

public record LoginRequest(
        String email,
        String password
) {}