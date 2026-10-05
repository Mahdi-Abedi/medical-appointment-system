package com.medapp.ui.view.auth;

import com.medapp.ui.dto.AuthResponse;
import com.medapp.ui.service.AuthService;
import com.medapp.ui.view.customer.DashboardView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("ورود | MedApp")
@PermitAll
public class LoginView extends VerticalLayout {

    public LoginView(@Autowired AuthService authService) {
        //setSizeFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setMargin(true);
        getStyle().set("background", "#f5f5f5");

        H1 title = new H1("ورود به MedApp");
        title.getStyle().set("color", "#7C3AED");

        EmailField email = new EmailField("ایمیل");
        email.setRequired(true);
        //email.setWidth("300px");

        PasswordField password = new PasswordField("رمز عبور");
        password.setRequired(true);
        //password.setWidth("300px");

        Button loginButton = new Button("ورود", e -> {
            try {
                AuthResponse response = authService.login(email.getValue(), password.getValue());
                Notification.show("خوش آمدید " + response.user().firstName() + "!", 3000,
                        Notification.Position.TOP_CENTER);
                UI.getCurrent().navigate(DashboardView.class);
            } catch (Exception ex) {
                Notification.show("خطا: ایمیل یا رمز عبور اشتباه است", 3000,
                        Notification.Position.MIDDLE);
            }
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        //loginButton.setWidth("300px");

        Button registerLink = new Button("حساب ندارید؟ ثبت‌نام کنید", e ->
                UI.getCurrent().navigate(RegisterView.class));
        registerLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

//        var form = new HorizontalLayout();
//        form.setWidth("320px");
//        form.add(email, password);

        add(title, email, password,
                loginButton, registerLink);
    }
}