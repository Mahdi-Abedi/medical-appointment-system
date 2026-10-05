package com.medapp.userservice.service;

import com.medapp.userservice.dto.AuthResponse;
import com.medapp.userservice.dto.LoginRequest;
import com.medapp.userservice.dto.RegisterRequest;
import com.medapp.userservice.dto.UserResponse;
import com.medapp.userservice.entity.Role;
import com.medapp.userservice.entity.User;
import com.medapp.userservice.exception.UserAlreadyExistsException;
import com.medapp.userservice.repository.UserRepository;
import com.medapp.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("کاربری با این ایمیل قبلاً ثبت‌نام کرده است");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .role(request.role() != null ? request.role() : Role.CUSTOMER)
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                86400L,
                UserResponse.fromEntity(user)
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                86400L,
                UserResponse.fromEntity(user)
        );
    }
}