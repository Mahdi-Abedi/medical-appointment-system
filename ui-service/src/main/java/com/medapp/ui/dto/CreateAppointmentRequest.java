package com.medapp.ui.dto;

import java.time.LocalDateTime;

public record CreateAppointmentRequest(
        Long userId,
        Long doctorId,
        LocalDateTime appointmentTime,
        Integer durationMinutes,
        String reason
) {}