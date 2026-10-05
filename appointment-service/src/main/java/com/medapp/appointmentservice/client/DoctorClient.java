package com.medapp.appointmentservice.client;

import com.medapp.appointmentservice.client.dto.DoctorDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/doctors")
public interface DoctorClient {

    @GetExchange("/{id}")
    DoctorDto getDoctorById(@PathVariable("id") Long id);
}