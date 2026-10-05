package com.medapp.ui.service;

import com.medapp.ui.client.AuthClient;
import com.medapp.ui.dto.AuthResponse;
import com.medapp.ui.dto.LoginRequest;
import com.medapp.ui.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthClient authClient;
    private final SessionService sessionService;
    private final SecurityContextRepository securityContextRepository;

    public AuthResponse login(String email, String password) {
        log.info("تلاش برای ورود با ایمیل: {}", email);
        AuthResponse response = authClient.login(new LoginRequest(email, password));
        sessionService.login(response);
        setSpringSecurityAuthentication(response);
        log.info("ورود موفق: {}", email);
        return response;
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("تلاش برای ثبت‌نام: {}", request.email());
        AuthResponse response = authClient.register(request);
        sessionService.login(response);
        setSpringSecurityAuthentication(response);
        log.info("ثبت‌نام و ورود موفق: {}", request.email());
        return response;
    }

    public void logout() {
        sessionService.logout();
        SecurityContextHolder.clearContext();
        log.info("خروج انجام شد");
    }

    private void setSpringSecurityAuthentication(AuthResponse response) {
        // ۱) ساخت Authentication
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + response.user().role())
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
                response.user().email(),
                null,
                authorities
        );

        // ۲) قرار دادن در SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // ۳) ذخیره در HttpSession از طریق SecurityContextRepository
        //    این خط حیاتی است — بدون آن، navigation بعدی کاربر را ناشناس می‌بیند.
        HttpServletRequest request = getCurrentRequest();
        HttpServletResponse response2 = getCurrentResponse();
        if (request != null && response2 != null) {
            securityContextRepository.saveContext(context, request, response2);
            log.debug("SecurityContext در HttpSession ذخیره شد");
        } else {
            log.warn("request/response در دسترس نبود — SecurityContext ذخیره نشد");
        }
    }

    private static HttpServletRequest getCurrentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private static HttpServletResponse getCurrentResponse() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getResponse();
        }
        return null;
    }
}