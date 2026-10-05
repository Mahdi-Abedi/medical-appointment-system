package com.medapp.appointmentservice.service;

import com.medapp.appointmentservice.client.DoctorClient;
import com.medapp.appointmentservice.client.UserClient;
import com.medapp.appointmentservice.client.dto.DoctorDto;
import com.medapp.appointmentservice.client.dto.UserDto;
import com.medapp.appointmentservice.dto.AppointmentResponse;
import com.medapp.appointmentservice.dto.CreateAppointmentRequest;
import com.medapp.appointmentservice.dto.UpdateAppointmentStatusRequest;
import com.medapp.appointmentservice.entity.Appointment;
import com.medapp.appointmentservice.entity.AppointmentStatus;
import com.medapp.appointmentservice.exception.AppointmentNotFoundException;
import com.medapp.appointmentservice.exception.ResourceNotFoundException;
import com.medapp.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserClient userClient;
    private final DoctorClient doctorClient;

    // ============================================================
    //                         ساخت نوبت
    // ============================================================
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {

        // ۱. اعتبارسنجی کاربر از طریق User Service
        UserDto user = validateUser(request.userId());

        // ۲. اعتبارسنجی پزشک از طریق Doctor Service
        DoctorDto doctor = validateDoctor(request.doctorId());

        // ۳. بررسی تداخل زمانی برای پزشک
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                request.doctorId(), request.appointmentTime());
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException(
                    "پزشک در این زمان نوبت دیگری دارد. لطفاً زمان دیگری انتخاب کنید");
        }

        // ۴. ساخت و ذخیره نوبت
        Appointment appointment = Appointment.builder()
                .userId(user.id())
                .doctorId(doctor.id())
                .appointmentTime(request.appointmentTime())
                .durationMinutes(request.durationMinutes())
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.reason())
                .consultationFee(doctor.consultationFee())
                .build();

        appointmentRepository.save(appointment);
        log.info("نوبت جدید ساخته شد: id={}, userId={}, doctorId={}",
                appointment.getId(), user.id(), doctor.id());

        return AppointmentResponse.fromEntity(appointment);
    }

    // ============================================================
    //                  دریافت یک نوبت با شناسه
    // ============================================================
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "نوبت با شناسه " + id + " یافت نشد"));
        return AppointmentResponse.fromEntity(appointment);
    }

    // ============================================================
    //            دریافت نوبت‌های یک کاربر خاص
    // ============================================================
    public List<AppointmentResponse> getAppointmentsByUser(Long userId) {
        // اعتبارسنجی وجود کاربر
        validateUser(userId);
        return appointmentRepository.findByUserIdOrderByAppointmentTimeDesc(userId).stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    // ============================================================
    //            دریافت نوبت‌های یک پزشک خاص
    // ============================================================
    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        // اعتبارسنجی وجود پزشک
        validateDoctor(doctorId);
        return appointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(doctorId).stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    // ============================================================
    //       دریافت نوبت‌های آینده‌ی یک کاربر (تاریخ > اکنون)
    // ============================================================
    public List<AppointmentResponse> getUpcomingAppointments(Long userId) {
        validateUser(userId);
        return appointmentRepository.findUpcomingByUser(userId, LocalDateTime.now()).stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    // ============================================================
    //              دریافت نوبت‌ها بر اساس وضعیت
    // ============================================================
    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    // ============================================================
    //                به‌روزرسانی وضعیت نوبت
    // ============================================================
    @Transactional
    public AppointmentResponse updateStatus(Long id, UpdateAppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "نوبت با شناسه " + id + " یافت نشد"));

        appointment.setStatus(request.status());
        if (request.notes() != null) {
            appointment.setNotes(request.notes());
        }

        appointmentRepository.save(appointment);
        log.info("وضعیت نوبت {} به {} تغییر یافت", id, request.status());

        return AppointmentResponse.fromEntity(appointment);
    }

    // ============================================================
    //                     لغو نوبت
    // ============================================================
    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "نوبت با شناسه " + id + " یافت نشد"));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("نوبت‌های تکمیل‌شده قابل لغو نیستند");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("این نوبت قبلاً لغو شده است");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        log.info("نوبت لغو شد: id={}", id);
    }

    // ============================================================
    //                    حذف کامل نوبت
    // ============================================================
    @Transactional
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new AppointmentNotFoundException("نوبت با شناسه " + id + " یافت نشد");
        }
        appointmentRepository.deleteById(id);
        log.info("نوبت حذف شد: id={}", id);
    }

    // ============================================================
    //                  متدهای کمکی اعتبارسنجی
    // ============================================================
    private UserDto validateUser(Long userId) {
        try {
            UserDto user = userClient.getUserById(userId);
            if (user == null || !user.enabled()) {
                throw new ResourceNotFoundException("کاربر با شناسه " + userId + " فعال نیست");
            }
            return user;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("کاربر با شناسه " + userId + " یافت نشد");
        } catch (RestClientResponseException e) {
            log.error("خطا در ارتباط با User Service: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResourceNotFoundException("خطا در اعتبارسنجی کاربر");
        }
    }

    private DoctorDto validateDoctor(Long doctorId) {
        try {
            DoctorDto doctor = doctorClient.getDoctorById(doctorId);
            if (doctor == null) {
                throw new ResourceNotFoundException("پزشک با شناسه " + doctorId + " یافت نشد");
            }
            if (!doctor.available()) {
                throw new IllegalStateException("این پزشک در حال حاضر نوبت نمی‌پذیرد");
            }
            return doctor;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("پزشک با شناسه " + doctorId + " یافت نشد");
        } catch (RestClientResponseException e) {
            log.error("خطا در ارتباط با Doctor Service: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResourceNotFoundException("خطا در اعتبارسنجی پزشک");
        }
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

}