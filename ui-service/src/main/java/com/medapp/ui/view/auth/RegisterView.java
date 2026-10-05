package com.medapp.ui.view.auth;

import com.medapp.ui.dto.RegisterRequest;
import com.medapp.ui.service.AuthService;
import com.medapp.ui.view.customer.DashboardView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("register")
@PageTitle("ثبت‌نام | MedApp")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(@Autowired AuthService authService) {
        setSizeFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("background", "#f5f5f5");

        H1 title = new H1("ثبت‌نام در MedApp");
        title.getStyle().set("color", "#7C3AED");

        TextField firstName = new TextField("نام");
        firstName.setRequired(true);

        TextField lastName = new TextField("نام خانوادگی");
        lastName.setRequired(true);

        EmailField email = new EmailField("ایمیل");
        email.setRequired(true);

        TextField phone = new TextField("شماره تماس");

        PasswordField password = new PasswordField("رمز عبور");
        password.setRequired(true);
        password.setHelperText("حداقل ۶ کاراکتر");

        PasswordField confirmPassword = new PasswordField("تکرار رمز عبور");
        confirmPassword.setRequired(true);

        FormLayout form = new FormLayout();
        form.setWidth("400px");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        form.add(firstName, lastName, email, phone, password, confirmPassword);

        Button registerButton = new Button("ثبت‌نام", e -> {
            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("رمز عبور و تکرار آن یکسان نیستند", 3000,
                        Notification.Position.MIDDLE);
                return;
            }
            try {
                RegisterRequest request = new RegisterRequest(
                        email.getValue(),
                        password.getValue(),
                        firstName.getValue(),
                        lastName.getValue(),
                        phone.getValue(),
                        "CUSTOMER"
                );
                authService.register(request);
                Notification.show("ثبت‌نام با موفقیت انجام شد!", 3000,
                        Notification.Position.TOP_CENTER);
                UI.getCurrent().navigate(DashboardView.class);
            } catch (Exception ex) {
                Notification.show("خطا در ثبت‌نام: " + ex.getMessage(), 3000,
                        Notification.Position.MIDDLE);
            }
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidth("400px");

        Button loginLink = new Button("حساب دارید؟ وارد شوید", e ->
                UI.getCurrent().navigate(LoginView.class));
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(title, form, registerButton, loginLink);
    }
}