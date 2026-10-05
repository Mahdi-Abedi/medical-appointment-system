package com.medapp.ui.service;

import com.medapp.ui.dto.AuthResponse;
import com.medapp.ui.dto.UserResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class SessionService {

    private String accessToken;
    private UserResponse currentUser;

    public void login(AuthResponse response) {
        this.accessToken = response.accessToken();
        this.currentUser = response.user();
    }

    public void logout() {
        this.accessToken = null;
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return accessToken != null && currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.role());
    }

//    public void setCurrentUser(UserResponse user) {
//        this.currentUser = user;
//    }
}