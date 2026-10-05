package com.medapp.appointmentservice.dto;

import com.medapp.appointmentservice.entity.Appointment;
import com.medapp.appointmentservice.entity.AppointmentStatus;

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
) {
    public static AppointmentResponse fromEntity(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getUserId(),
                a.getDoctorId(),
                a.getAppointmentTime(),
                a.getDurationMinutes(),
                a.getStatus(),
                a.getReason(),
                a.getNotes(),
                a.getConsultationFee(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}