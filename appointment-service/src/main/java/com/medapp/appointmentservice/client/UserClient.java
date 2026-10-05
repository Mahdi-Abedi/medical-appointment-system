package com.medapp.appointmentservice.client;

import com.medapp.appointmentservice.client.dto.UserDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/users")
public interface UserClient {

    @GetExchange("/{id}")
    UserDto getUserById(@PathVariable Long id);
}
