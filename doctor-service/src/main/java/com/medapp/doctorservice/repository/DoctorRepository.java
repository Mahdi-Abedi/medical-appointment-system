package com.medapp.doctorservice.repository;

import com.medapp.doctorservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Doctor> findBySpecializationIgnoreCase(String specialization);

    List<Doctor> findByAvailableTrue();

    List<Doctor> findBySpecializationIgnoreCaseAndAvailableTrue(String specialization);
}