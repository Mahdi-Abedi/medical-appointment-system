package com.medapp.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.service.registry.ImportHttpServices;

import java.util.Locale;

@SpringBootApplication
@ImportHttpServices(group = "auth-client", types = com.medapp.ui.client.AuthClient.class)
@ImportHttpServices(group = "user-client", types = com.medapp.ui.client.UserClient.class)
@ImportHttpServices(group = "doctor-client", types = com.medapp.ui.client.DoctorClient.class)
@ImportHttpServices(group = "appointment-client", types = com.medapp.ui.client.AppointmentClient.class)
public class UiServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("fa-IR"));
        SpringApplication.run(UiServiceApplication.class, args);
    }

}
