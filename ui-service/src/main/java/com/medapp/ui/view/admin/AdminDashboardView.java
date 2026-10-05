package com.medapp.ui.view.admin;

import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.AppointmentService;
import com.medapp.ui.service.DoctorService;
import com.medapp.ui.service.UserService;
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
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("پنل مدیریت | MedApp")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    private final UserService userService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public AdminDashboardView(UserService userService,
                              DoctorService doctorService,
                              AppointmentService appointmentService) {
        this.userService = userService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");

        add(
                createHeader(),
                createStatsRow(),
                createQuickActions()
        );
    }

    private Component createHeader() {
        H2 title = new H2("پنل مدیریت");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("مدیریت کاربران، پزشکان و نوبت‌ها");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("gap", "2px");
        return header;
    }

    private Component createStatsRow() {
        long userCount = userService.getAllUsers().size();
        long doctorCount = doctorService.getAllDoctors().size();

        HorizontalLayout row = new HorizontalLayout(
                createStatCard("کاربران", userCount, VaadinIcon.USERS, "#7C3AED"),
                createStatCard("پزشکان", doctorCount, VaadinIcon.DOCTOR, "#10B981"),
                createStatCard("نوبت‌ها", 0, VaadinIcon.CALENDAR, "#F59E0B")
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("gap", "16px").set("flex-wrap", "wrap");
        return row;
    }

    private Component createStatCard(String title, long value, VaadinIcon icon, String color) {
        Icon iconComp = icon.create();
        iconComp.setSize("24px");
        iconComp.getStyle().set("color", color);

        VerticalLayout iconWrap = new VerticalLayout(iconComp);
        iconWrap.setAlignItems(FlexComponent.Alignment.CENTER);
        iconWrap.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        iconWrap.setPadding(false);
        iconWrap.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("border-radius", "12px")
                .set("background", color + "20")
                .set("flex-shrink", "0");

        Span valueSpan = new Span(toPersianDigits(String.valueOf(value)));
        valueSpan.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-size", "13px").set("color", "#6B7280");

        VerticalLayout textCol = new VerticalLayout(valueSpan, titleSpan);
        textCol.setPadding(false);
        textCol.setSpacing(false);
        textCol.getStyle().set("gap", "2px");

        HorizontalLayout content = new HorizontalLayout(iconWrap, textCol);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.getStyle().set("gap", "12px");

        VerticalLayout card = new VerticalLayout(content);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("flex", "1")
                .set("min-width", "220px");
        return card;
    }

    private Component createQuickActions() {
        H3 title = new H3("دسترسی سریع");
        title.getStyle()
                .set("margin", "8px 0 4px 0")
                .set("font-size", "18px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        Button users = new Button("مدیریت کاربران", VaadinIcon.USERS.create());
        users.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        users.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("admin/users")));

        Button doctors = new Button("مدیریت پزشکان", VaadinIcon.DOCTOR.create());
        doctors.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        doctors.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("admin/doctors")));

        Button appointments = new Button("همه‌ی نوبت‌ها", VaadinIcon.CALENDAR.create());
        appointments.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        appointments.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("admin/appointments")));

        HorizontalLayout buttons = new HorizontalLayout(users, doctors, appointments);
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