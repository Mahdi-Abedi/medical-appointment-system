package com.medapp.doctorservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateDoctorRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone,
        @NotBlank String specialization,
        String bio,
        @NotNull @Min(0) Integer yearsOfExperience,
        BigDecimal consultationFee,
        Boolean available
) {
}