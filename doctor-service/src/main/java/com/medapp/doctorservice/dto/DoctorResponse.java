package com.medapp.doctorservice.dto;

import com.medapp.doctorservice.entity.Doctor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DoctorResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String specialization,
        String bio,
        Integer yearsOfExperience,
        BigDecimal consultationFee,
        boolean available,
        LocalDateTime createdAt
) {
    public static DoctorResponse fromEntity(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getSpecialization(),
                doctor.getBio(),
                doctor.getYearsOfExperience(),
                doctor.getConsultationFee(),
                doctor.isAvailable(),
                doctor.getCreatedAt()
        );
    }
}