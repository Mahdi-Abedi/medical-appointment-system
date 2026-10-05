package com.medapp.appointmentservice.repository;

import com.medapp.appointmentservice.entity.Appointment;
import com.medapp.appointmentservice.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByAppointmentTimeDesc(Long userId);

    List<Appointment> findByDoctorIdOrderByAppointmentTimeDesc(Long doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
            "AND a.appointmentTime = :time " +
            "AND a.status IN ('SCHEDULED', 'CONFIRMED')")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("time") LocalDateTime time
    );

    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId " +
            "AND a.appointmentTime > :now " +
            "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentTime")
    List<Appointment> findUpcomingByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}