package com.medapp.ui.service;

import com.medapp.ui.client.AppointmentClient;
import com.medapp.ui.dto.AppointmentResponse;
import com.medapp.ui.dto.AppointmentStatus;
import com.medapp.ui.dto.CreateAppointmentRequest;
import com.medapp.ui.dto.UpdateAppointmentStatusRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentClient appointmentClient;

    // ============================================
    // ایجاد نوبت
    // ============================================
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        log.info("Creating appointment: user={}, doctor={}, time={}",
                request.userId(), request.doctorId(), request.appointmentTime());
        return appointmentClient.create(request);
    }

    // ============================================
    // یک نوبت
    // ============================================
    public Optional<AppointmentResponse> getById(Long id) {
        try {
            return Optional.ofNullable(appointmentClient.getById(id));
        } catch (Exception ex) {
            log.error("Failed to fetch appointment {}", id, ex);
            return Optional.empty();
        }
    }

    // ============================================
    // نوبت‌های کاربر
    // ============================================
    public List<AppointmentResponse> getMyAppointments(Long userId) {
        try {
            return appointmentClient.getByUser(userId);
        } catch (Exception ex) {
            log.error("Failed to fetch appointments for user {}", userId, ex);
            return Collections.emptyList();
        }
    }

    public List<AppointmentResponse> getMyUpcomingAppointments(Long userId) {
        try {
            return appointmentClient.getUpcoming(userId);
        } catch (Exception ex) {
            log.error("Failed to fetch upcoming appointments for user {}", userId, ex);
            return Collections.emptyList();
        }
    }

    // ============================================
    // نوبت‌های پزشک (برای Admin)
    // ============================================
    public List<AppointmentResponse> getByDoctor(Long doctorId) {
        try {
            return appointmentClient.getByDoctor(doctorId);
        } catch (Exception ex) {
            log.error("Failed to fetch appointments for doctor {}", doctorId, ex);
            return Collections.emptyList();
        }
    }

    // ============================================
    // تغییر وضعیت
    // ============================================
    public AppointmentResponse updateStatus(Long id, AppointmentStatus status, String notes) {
        return appointmentClient.updateStatus(id, new UpdateAppointmentStatusRequest(status, notes));
    }

    public AppointmentResponse updateStatus(Long id, AppointmentStatus status) {
        return updateStatus(id, status, null);
    }

    // ============================================
    // لغو نوبت
    // ============================================
    public void cancel(Long id) {
        appointmentClient.cancel(id);
    }

    // ============================================
    // کمکی برای Dashboard
    // ============================================
    public long countMyAppointments(Long userId) {
        return getMyAppointments(userId).size();
    }

    public long countMyUpcomingAppointments(Long userId) {
        return getMyUpcomingAppointments(userId).size();
    }

    public List<AppointmentResponse> getAllAppointments() {
        try {
            return appointmentClient.getAll();
        } catch (Exception ex) {
            log.error("Failed to fetch all appointments", ex);
            return Collections.emptyList();
        }
    }
    
}