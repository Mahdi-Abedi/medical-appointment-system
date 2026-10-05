package com.medapp.ui.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long userId,
        Long doctorId,
        LocalDateTime appointmentTime,
        Integer durationMinutes,
        AppointmentStatus status,
        String reason,
        String notes,
        BigDecimal consultationFee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}