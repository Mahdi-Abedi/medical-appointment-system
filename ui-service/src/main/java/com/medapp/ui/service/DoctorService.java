package com.medapp.ui.service;

import com.medapp.ui.client.DoctorClient;
import com.medapp.ui.dto.CreateDoctorRequest;
import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.dto.UpdateDoctorRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorClient doctorClient;

    // ============================================
    // خواندن لیست همه‌ی پزشکان
    // ============================================
    public List<DoctorResponse> getAllDoctors() {
        try {
            List<DoctorResponse> doctors = doctorClient.getAllDoctors();
            log.debug("{} doctor(s) fetched", doctors.size());
            return doctors;
        } catch (Exception ex) {
            log.error("Failed to fetch doctors", ex);
            return Collections.emptyList();
        }
    }

    // ============================================
    // فقط پزشکان available
    // ============================================
    public List<DoctorResponse> getAvailableDoctors() {
        try {
            return doctorClient.getAvailableDoctors();
        } catch (Exception ex) {
            log.error("Failed to fetch available doctors", ex);
            return Collections.emptyList();
        }
    }

    // ============================================
    // یک پزشک با ID
    // ============================================
    public Optional<DoctorResponse> getDoctorById(Long id) {
        try {
            return Optional.ofNullable(doctorClient.getDoctorById(id));
        } catch (Exception ex) {
            log.error("Failed to fetch doctor with id {}", id, ex);
            return Optional.empty();
        }
    }

    // ============================================
    // جستجو بر اساس تخصص
    // ============================================
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            return getAllDoctors();
        }
        try {
            return doctorClient.getDoctorsBySpecialization(specialization);
        } catch (Exception ex) {
            log.error("Failed to fetch doctors by specialization {}", specialization, ex);
            return Collections.emptyList();
        }
    }

    // ============================================
    // کمکی: تعداد پزشکان موجود (برای Dashboard)
    // ============================================
    public long countAvailableDoctors() {
        return getAvailableDoctors().size();
    }

    // ============================================
    // کمکی: استخراج لیست تخصص‌های یکتا (برای فیلتر)
    // ============================================
    public List<String> getAllSpecializations() {
        return getAllDoctors().stream()
                .map(DoctorResponse::specialization)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        log.info("Creating doctor: {} {}", request.firstName(), request.lastName());
        return doctorClient.create(request);
    }

    public DoctorResponse updateDoctor(Long id, UpdateDoctorRequest request) {
        log.info("Updating doctor {}", id);
        return doctorClient.update(id, request);
    }

    public void deleteDoctor(Long id) {
        log.info("Deleting doctor {}", id);
        doctorClient.delete(id);
    }
}