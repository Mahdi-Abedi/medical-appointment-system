package com.medapp.doctorservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateDoctorRequest(
        @NotBlank(message = "نام نمی‌تواند خالی باشد")
        String firstName,

        @NotBlank(message = "نام خانوادگی نمی‌تواند خالی باشد")
        String lastName,

        @NotBlank(message = "ایمیل نمی‌تواند خالی باشد")
        @Email(message = "فرمت ایمیل نامعتبر است")
        String email,

        String phone,

        @NotBlank(message = "تخصص نمی‌تواند خالی باشد")
        String specialization,

        String bio,

        @NotNull(message = "سابقه کار نمی‌تواند خالی باشد")
        @Min(value = 0, message = "سابقه کار نمی‌تواند منفی باشد")
        Integer yearsOfExperience,

        @DecimalMin(value = "0.0", inclusive = false, message = "هزینه ویزیت باید مثبت باشد")
        BigDecimal consultationFee
) {
}