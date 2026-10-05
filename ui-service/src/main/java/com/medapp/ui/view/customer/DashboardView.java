package com.medapp.ui.view.customer;

import com.medapp.ui.dto.UserResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.DoctorService;
import com.medapp.ui.service.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("داشبورد | MedApp")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final SessionService sessionService;
    private final DoctorService doctorService;

    public DashboardView(SessionService sessionService, DoctorService doctorService) {
        this.sessionService = sessionService;
        this.doctorService = doctorService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");

        add(
                createWelcomeCard(),
                createStatsRow(),
                createQuickActions()
        );
    }

    // ============================================
    // ۱) کارت خوشآمد
    // ============================================
    private Component createWelcomeCard() {
        UserResponse user = sessionService.getCurrentUser();
        String name = (user != null) ? user.firstName() + " " + user.lastName() : "کاربر";

        H2 greeting = new H2("سلام، " + name + " 👋");
        greeting.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("به سیستم نوبت‌دهی پزشکی MedApp خوش آمدید.");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        VerticalLayout content = new VerticalLayout(greeting, subtitle);
        content.setPadding(false);
        content.setSpacing(false);

        VerticalLayout card = new VerticalLayout(content);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)")
                .set("border-radius", "16px")
                .set("padding", "28px 32px")
                .set("color", "white")
                .set("box-shadow", "0 4px 12px rgba(124, 58, 237, 0.2)");

        // متن داخل کارت سفید شود
        greeting.getStyle().set("color", "white");
        subtitle.getStyle().set("color", "rgba(255, 255, 255, 0.85)");

        return card;
    }

    // ============================================
    // ۲) کارت‌های آماری (فعلاً صفر — در فاز ۳ واقعی می‌شود)
    // ============================================
    private Component createStatsRow() {
        long availableCount = doctorService.countAvailableDoctors();
        String availableCountStr = toPersianDigits(String.valueOf(availableCount));

        HorizontalLayout row = new HorizontalLayout(
                createStatCard("نوبت‌های من", "۰", VaadinIcon.CALENDAR, "#7C3AED"),
                createStatCard("پزشکان موجود", availableCountStr, VaadinIcon.DOCTOR, "#10B981"),
                createStatCard("نوبت‌های آینده", "۰", VaadinIcon.CLOCK, "#F59E0B")
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("gap", "16px");
        row.setFlexGrow(1, row.getChildren().toArray(Component[]::new));
        return row;
    }

    private Component createStatCard(String title, String value, VaadinIcon icon, String color) {
        Icon iconComp = icon.create();
        iconComp.setSize("24px");
        iconComp.getStyle().set("color", color);

        HorizontalLayout iconWrap = new HorizontalLayout(iconComp);
        iconWrap.setAlignItems(FlexComponent.Alignment.CENTER);
        iconWrap.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        iconWrap.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("border-radius", "12px")
                .set("background", color + "20"); // شفافیت ۲۰٪

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937")
                .set("line-height", "1.2");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "13px")
                .set("color", "#6B7280");

        VerticalLayout textCol = new VerticalLayout(valueSpan, titleSpan);
        textCol.setPadding(false);
        textCol.setSpacing(false);
        textCol.getStyle().set("gap", "2px");

        HorizontalLayout content = new HorizontalLayout(iconWrap, textCol);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);

        VerticalLayout card = new VerticalLayout(content);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("flex", "1")
                .set("min-width", "200px");

        return card;
    }

    // ============================================
    // ۳) دسترسی سریع
    // ============================================
    private Component createQuickActions() {
        H3 title = new H3("دسترسی سریع");
        title.getStyle()
                .set("margin", "8px 0 4px 0")
                .set("font-size", "18px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        Button browseDoctors = new Button("مشاهده‌ی پزشکان", VaadinIcon.DOCTOR.create());
        browseDoctors.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        browseDoctors.setWidth("200px");
        browseDoctors.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("doctors")));

        Button myAppointments = new Button("نوبت‌های من", VaadinIcon.CALENDAR.create());
        myAppointments.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        myAppointments.setWidth("200px");
        myAppointments.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("appointments")));

        Button profile = new Button("پروفایل من", VaadinIcon.USER.create());
        profile.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        profile.setWidth("200px");
        profile.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("profile")));
        
        HorizontalLayout buttons = new HorizontalLayout(browseDoctors, myAppointments, profile);
        buttons.setSpacing(true);
        buttons.getStyle().set("gap", "12px").set("flex-wrap", "wrap");

        VerticalLayout layout = new VerticalLayout(title, buttons);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "12px");

        return layout;
    }
    private static String toPersianDigits(String input) {
        if (input == null) return "";
        char[] persian = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append(persian[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}