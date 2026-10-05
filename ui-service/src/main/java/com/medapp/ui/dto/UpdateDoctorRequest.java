package com.medapp.ui.dto;

import java.math.BigDecimal;

public record UpdateDoctorRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String specialization,
        String bio,
        Integer yearsOfExperience,
        BigDecimal consultationFee,
        Boolean available
) {}