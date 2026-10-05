package com.medapp.appointmentservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateAppointmentRequest(
        @NotNull(message = "شناسه کاربر الزامی است")
        Long userId,

        @NotNull(message = "شناسه پزشک الزامی است")
        Long doctorId,

        @NotNull(message = "زمان نوبت الزامی است")
        @Future(message = "زمان نوبت باید در آینده باشد")
        LocalDateTime appointmentTime,

        @NotNull(message = "مدت نوبت الزامی است")
        Integer durationMinutes,

        @Size(max = 500, message = "دلیل نمی‌تواند بیش از ۵۰۰ کاراکتر باشد")
        String reason
) {
}