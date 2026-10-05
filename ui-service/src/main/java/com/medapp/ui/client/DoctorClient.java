package com.medapp.ui.client;

import com.medapp.ui.dto.CreateDoctorRequest;
import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.dto.UpdateDoctorRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

import java.util.List;

@HttpExchange(url = "/api/doctors")
public interface DoctorClient {

    @GetExchange
    List<DoctorResponse> getAllDoctors();

    @GetExchange("/{id}")
    DoctorResponse getDoctorById(@PathVariable("id") Long id);

    @GetExchange("/available")
    List<DoctorResponse> getAvailableDoctors();

    @GetExchange("/specialization/{specialization}")
    List<DoctorResponse> getDoctorsBySpecialization(
            @PathVariable("specialization") String specialization);

    @PostExchange
    DoctorResponse create(@RequestBody CreateDoctorRequest request);

    @PutExchange("/{id}")
    DoctorResponse update(
            @PathVariable("id") Long id,
            @RequestBody UpdateDoctorRequest request);

    @DeleteExchange("/{id}")
    void delete(@PathVariable("id") Long id);
}