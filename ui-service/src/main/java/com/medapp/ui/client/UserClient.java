package com.medapp.ui.client;

import com.medapp.ui.dto.UpdateUserRequest;
import com.medapp.ui.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@HttpExchange(url = "/api/users")
public interface UserClient {

    @GetExchange("/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);

    @GetExchange("/me")
    UserResponse getCurrentUser();

    @GetExchange
    List<UserResponse> getAllUsers();

    @PutExchange("/{id}")
    UserResponse updateUser(
            @PathVariable("id") Long id,
            @RequestBody UpdateUserRequest request);

    @DeleteExchange("/{id}")
    void deleteUser(@PathVariable("id") Long id);
}