package com.medapp.appointmentservice.client.dto;

import java.math.BigDecimal;

public record DoctorDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String specialization,
        BigDecimal consultationFee,
        boolean available
) {
}