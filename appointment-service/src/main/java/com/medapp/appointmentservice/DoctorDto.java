package com.medapp.appointmentservice;

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