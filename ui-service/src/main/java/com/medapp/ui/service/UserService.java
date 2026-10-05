package com.medapp.ui.service;

import com.medapp.ui.client.UserClient;
import com.medapp.ui.dto.UpdateUserRequest;
import com.medapp.ui.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserClient userClient;

    public Optional<UserResponse> getUserById(Long id) {
        try {
            return Optional.ofNullable(userClient.getUserById(id));
        } catch (Exception ex) {
            log.error("Failed to fetch user {}", id, ex);
            return Optional.empty();
        }
    }

    public Optional<UserResponse> getCurrentUser() {
        try {
            return Optional.ofNullable(userClient.getCurrentUser());
        } catch (Exception ex) {
            log.error("Failed to fetch current user", ex);
            return Optional.empty();
        }
    }

    public List<UserResponse> getAllUsers() {
        try {
            return userClient.getAllUsers();
        } catch (Exception ex) {
            log.error("Failed to fetch all users", ex);
            return Collections.emptyList();
        }
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        return userClient.updateUser(id, request);
    }

    public void deleteUser(Long id) {
        userClient.deleteUser(id);
    }
}