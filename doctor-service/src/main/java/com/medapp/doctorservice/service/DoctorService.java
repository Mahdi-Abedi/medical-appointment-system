package com.medapp.doctorservice.service;

import com.medapp.doctorservice.dto.CreateDoctorRequest;
import com.medapp.doctorservice.dto.DoctorResponse;
import com.medapp.doctorservice.dto.UpdateDoctorRequest;
import com.medapp.doctorservice.entity.Doctor;
import com.medapp.doctorservice.exception.DoctorNotFoundException;
import com.medapp.doctorservice.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        if (doctorRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("پزشکی با این ایمیل قبلاً ثبت شده است");
        }

        Doctor doctor = Doctor.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .specialization(request.specialization())
                .bio(request.bio())
                .yearsOfExperience(request.yearsOfExperience())
                .consultationFee(request.consultationFee())
                .available(true)
                .build();

        doctorRepository.save(doctor);
        return DoctorResponse.fromEntity(doctor);
    }

    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("پزشک با شناسه " + id + " یافت نشد"));
        return DoctorResponse.fromEntity(doctor);
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(DoctorResponse::fromEntity)
                .toList();
    }

    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationIgnoreCaseAndAvailableTrue(specialization).stream()
                .map(DoctorResponse::fromEntity)
                .toList();
    }

    public List<DoctorResponse> getAvailableDoctors() {
        return doctorRepository.findByAvailableTrue().stream()
                .map(DoctorResponse::fromEntity)
                .toList();
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("پزشک با شناسه " + id + " یافت نشد"));

        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setPhone(request.phone());
        doctor.setSpecialization(request.specialization());
        doctor.setBio(request.bio());
        doctor.setYearsOfExperience(request.yearsOfExperience());
        doctor.setConsultationFee(request.consultationFee());
        if (request.available() != null) {
            doctor.setAvailable(request.available());
        }

        doctorRepository.save(doctor);
        return DoctorResponse.fromEntity(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new DoctorNotFoundException("پزشک با شناسه " + id + " یافت نشد");
        }
        doctorRepository.deleteById(id);
    }
}