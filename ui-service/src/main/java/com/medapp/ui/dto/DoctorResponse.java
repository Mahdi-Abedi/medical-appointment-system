package com.medapp.ui.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DoctorResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String specialization,
        String bio,
        Integer yearsOfExperience,
        BigDecimal consultationFee,
        boolean available,
        LocalDateTime createdAt
) {}