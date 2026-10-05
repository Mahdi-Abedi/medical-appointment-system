package com.medapp.userservice.dto;

import com.medapp.userservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "ایمیل نمی‌تواند خالی باشد")
        @Email(message = "فرمت ایمیل نامعتبر است")
        String email,

        @NotBlank(message = "رمز عبور نمی‌تواند خالی باشد")
        @Size(min = 6, message = "رمز عبور باید حداقل ۶ کاراکتر باشد")
        String password,

        @NotBlank(message = "نام نمی‌تواند خالی باشد")
        String firstName,

        @NotBlank(message = "نام خانوادگی نمی‌تواند خالی باشد")
        String lastName,

        String phone,

        Role role
) {}