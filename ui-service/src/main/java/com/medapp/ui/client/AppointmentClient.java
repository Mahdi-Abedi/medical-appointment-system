package com.medapp.ui.client;

import com.medapp.ui.dto.AppointmentResponse;
import com.medapp.ui.dto.CreateAppointmentRequest;
import com.medapp.ui.dto.UpdateAppointmentStatusRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange(url = "/api/appointments")
public interface AppointmentClient {

    @PostExchange
    AppointmentResponse create(@RequestBody CreateAppointmentRequest request);

    @GetExchange("/{id}")
    AppointmentResponse getById(@PathVariable("id") Long id);

    @GetExchange("/user/{userId}")
    List<AppointmentResponse> getByUser(@PathVariable("userId") Long userId);

    @GetExchange("/user/{userId}/upcoming")
    List<AppointmentResponse> getUpcoming(@PathVariable("userId") Long userId);

    @GetExchange("/doctor/{doctorId}")
    List<AppointmentResponse> getByDoctor(@PathVariable("doctorId") Long doctorId);

    @PatchExchange("/{id}/status")
    AppointmentResponse updateStatus(
            @PathVariable("id") Long id,
            @RequestBody UpdateAppointmentStatusRequest request);

    @DeleteExchange("/{id}")
    void cancel(@PathVariable("id") Long id);

    @GetExchange
    List<AppointmentResponse> getAll();
}