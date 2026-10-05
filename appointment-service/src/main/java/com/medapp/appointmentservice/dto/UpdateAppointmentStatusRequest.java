package com.medapp.appointmentservice.dto;

import com.medapp.appointmentservice.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull(message = "وضعیت الزامی است")
        AppointmentStatus status,

        String notes
) {
}