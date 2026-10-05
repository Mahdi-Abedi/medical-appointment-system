package com.medapp.ui.client;

import com.medapp.ui.dto.AuthResponse;
import com.medapp.ui.dto.LoginRequest;
import com.medapp.ui.dto.RegisterRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "/api/auth")
public interface AuthClient {

    @PostExchange("/register")
    AuthResponse register(@RequestBody RegisterRequest request);

    @PostExchange("/login")
    AuthResponse login(@RequestBody LoginRequest request);
}