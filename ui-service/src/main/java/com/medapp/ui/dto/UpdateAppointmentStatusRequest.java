package com.medapp.ui.dto;

public record UpdateAppointmentStatusRequest(
        AppointmentStatus status,
        String notes
) {}