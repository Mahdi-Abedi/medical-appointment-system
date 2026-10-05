package com.medapp.ui.view.customer;

import com.medapp.ui.dto.AppointmentResponse;
import com.medapp.ui.dto.AppointmentStatus;
import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.AppointmentService;
import com.medapp.ui.service.DoctorService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Route(value = "appointments/:appointmentId", layout = MainLayout.class)
@PageTitle("جزئیات نوبت | MedApp")
@PermitAll
public class AppointmentDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    private Long appointmentId;
    private AppointmentResponse appointment;
    private DoctorResponse doctor;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FULL_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public AppointmentDetailView(AppointmentService appointmentService,
                                 DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("appointmentId").orElse(null);
        if (idParam == null) {
            event.rerouteTo(MyAppointmentsView.class);
            return;
        }
        try {
            this.appointmentId = Long.parseLong(idParam);
        } catch (NumberFormatException ex) {
            event.rerouteTo(MyAppointmentsView.class);
            return;
        }

        Optional<AppointmentResponse> result = appointmentService.getById(appointmentId);
        if (result.isEmpty()) {
            renderNotFound();
            return;
        }
        this.appointment = result.get();

        if (appointment.doctorId() != null) {
            doctorService.getDoctorById(appointment.doctorId())
                    .ifPresent(d -> this.doctor = d);
        }

        renderContent();
    }

    // ============================================
    // محتوای اصلی
    // ============================================
    private void renderContent() {
        removeAll();

        add(
                createBackButton(),
                createTitleRow(),
                createStatusTimeline(),
                createMainGrid(),
                createReasonAndNotes(),
                createActionButtons()
        );
    }

    // ============================================
    // دکمه بازگشت
    // ============================================
    private Component createBackButton() {
        Button back = new Button("بازگشت به نوبت‌های من", VaadinIcon.ARROW_RIGHT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("margin-bottom", "8px");
        back.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("appointments")));
        return back;
    }

    // ============================================
    // تیتر صفحه با badge وضعیت
    // ============================================
    private Component createTitleRow() {
        H2 title = new H2("جزئیات نوبت #" + toPersianDigits(String.valueOf(appointment.id())));
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Span badge = createStatusBadge(appointment.status());

        HorizontalLayout row = new HorizontalLayout(title, badge);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("gap", "12px");
        return row;
    }

    // ============================================
    // تایم‌لاین وضعیت
    // ============================================
    private Component createStatusTimeline() {
        VerticalLayout timeline = new VerticalLayout();
        timeline.setPadding(true);
        timeline.setSpacing(false);
        timeline.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("gap", "8px");

        H3 label = new H3("وضعیت نوبت");
        label.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("color", "#374151");
        timeline.add(label);

        // مراحل
        AppointmentStatus s = appointment.status();

        // اگر لغو شده، فقط SCHEDULED و CANCELLED را نشان بده
        if (s == AppointmentStatus.CANCELLED) {
            timeline.add(createStep("برنامه‌ریزی‌شده", true, true));
            timeline.add(createConnector(true));
            timeline.add(createStep("لغو شده", true, false));
            return timeline;
        }

        boolean scheduledDone = true;
        boolean confirmedDone = (s == AppointmentStatus.CONFIRMED
                || s == AppointmentStatus.COMPLETED
                || s == AppointmentStatus.NO_SHOW);
        boolean completedDone = (s == AppointmentStatus.COMPLETED);

        timeline.add(createStep("برنامه‌ریزی‌شده", scheduledDone, !confirmedDone));
        timeline.add(createConnector(confirmedDone));
        timeline.add(createStep("تأییدشده", confirmedDone, !completedDone));
        timeline.add(createConnector(completedDone));
        timeline.add(createStep("انجام‌شده", completedDone, completedDone));

        return timeline;
    }

    private Component createStep(String label, boolean done, boolean current) {
        Span circle = new Span();
        String color = done ? "#7C3AED" : "#E5E7EB";
        String textColor = done ? "white" : "#9CA3AF";

        circle.getStyle()
                .set("width", "24px")
                .set("height", "24px")
                .set("border-radius", "50%")
                .set("background", color)
                .set("color", textColor)
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("flex-shrink", "0");

        if (done) {
            Icon check = VaadinIcon.CHECK.create();
            check.setSize("14px");
            check.getStyle().set("color", "white");
            circle.add(check);
        }

        Span text = new Span(label);
        text.getStyle()
                .set("font-size", "14px")
                .set("font-weight", current ? "700" : "500")
                .set("color", done ? "#1F2937" : "#9CA3AF");

        HorizontalLayout row = new HorizontalLayout(circle, text);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("gap", "12px");
        return row;
    }

    private Component createConnector(boolean done) {
        Div connector = new Div();
        connector.getStyle()
                .set("width", "2px")
                .set("height", "20px")
                .set("background", done ? "#7C3AED" : "#E5E7EB")
                .set("margin-right", "11px")
                .set("margin-top", "2px")
                .set("margin-bottom", "2px");
        return connector;
    }

    // ============================================
    // گرید کارت‌های اصلی
    // ============================================
    private Component createMainGrid() {
        HorizontalLayout row = new HorizontalLayout(
                createDateTimeCard(),
                createDoctorCard()
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("gap", "16px").set("flex-wrap", "wrap");
        return row;
    }

    // ============================================
    // کارت تاریخ/ساعت
    // ============================================
    private Component createDateTimeCard() {
        H3 title = new H3("زمان نوبت");
        title.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("color", "#374151");

        LocalDateTime dt = appointment.appointmentTime();

        VerticalLayout content = new VerticalLayout(
                createInfoRow(VaadinIcon.CALENDAR, "تاریخ",
                        toPersianDigits(dt.format(DATE_FMT)), "#7C3AED"),
                createInfoRow(VaadinIcon.CLOCK, "ساعت",
                        toPersianDigits(dt.format(TIME_FMT)), "#F59E0B"),
                createInfoRow(VaadinIcon.TIMER, "مدت",
                        appointment.durationMinutes() != null
                                ? toPersianDigits(appointment.durationMinutes().toString()) + " دقیقه"
                                : "—",
                        "#10B981")
        );
        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("gap", "12px");

        VerticalLayout card = new VerticalLayout(title, content);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("flex", "1")
                .set("min-width", "300px");
        return card;
    }

    // ============================================
    // کارت پزشک
    // ============================================
    private Component createDoctorCard() {
        H3 title = new H3("پزشک");
        title.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("color", "#374151");

        VerticalLayout content = new VerticalLayout();

        if (doctor != null) {
            String initials = getInitials(doctor.firstName(), doctor.lastName());
            Span avatar = new Span(initials);
            avatar.getStyle()
                    .set("width", "48px")
                    .set("height", "48px")
                    .set("border-radius", "50%")
                    .set("background", "linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)")
                    .set("color", "white")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("font-size", "18px")
                    .set("font-weight", "700")
                    .set("flex-shrink", "0");

            Span name = new Span("دکتر " + doctor.firstName() + " " + doctor.lastName());
            name.getStyle()
                    .set("font-size", "15px")
                    .set("font-weight", "600")
                    .set("color", "#1F2937");

            Span spec = new Span(doctor.specialization() != null ? doctor.specialization() : "—");
            spec.getStyle()
                    .set("font-size", "13px")
                    .set("color", "#7C3AED");

            VerticalLayout textCol = new VerticalLayout(name, spec);
            textCol.setPadding(false);
            textCol.setSpacing(false);
            textCol.getStyle().set("gap", "2px");

            HorizontalLayout doctorRow = new HorizontalLayout(avatar, textCol);
            doctorRow.setAlignItems(FlexComponent.Alignment.CENTER);
            doctorRow.setSpacing(true);
            doctorRow.getStyle().set("gap", "12px");

            Button viewDoctor = new Button("مشاهده پروفایل", VaadinIcon.EXTERNAL_LINK.create());
            viewDoctor.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewDoctor.addClickListener(e ->
                    getUI().ifPresent(ui -> ui.navigate("doctors/" + doctor.id())));

            content.add(doctorRow, viewDoctor);
        } else {
            Span unknown = new Span("پزشک #" + appointment.doctorId());
            unknown.getStyle().set("color", "#6B7280").set("font-size", "14px");
            content.add(unknown);
        }

        if (appointment.consultationFee() != null) {
            content.add(createInfoRow(VaadinIcon.MONEY, "هزینه ویزیت",
                    formatFee(appointment.consultationFee()) + " تومان",
                    "#10B981"));
        }

        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("gap", "12px");

        VerticalLayout card = new VerticalLayout(title, content);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("flex", "1")
                .set("min-width", "300px");
        return card;
    }

    // ============================================
    // ردیف اطلاعات
    // ============================================
    private Component createInfoRow(VaadinIcon icon, String label, String value, String color) {
        Icon iconComp = icon.create();
        iconComp.setSize("18px");
        iconComp.getStyle().set("color", color);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#6B7280");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        VerticalLayout textCol = new VerticalLayout(labelSpan, valueSpan);
        textCol.setPadding(false);
        textCol.setSpacing(false);
        textCol.getStyle().set("gap", "2px");

        HorizontalLayout row = new HorizontalLayout(iconComp, textCol);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("gap", "10px");
        return row;
    }

    // ============================================
    // دلیل و یادداشت‌ها
    // ============================================
    private Component createReasonAndNotes() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("gap", "16px");

        if (appointment.reason() != null && !appointment.reason().isBlank()) {
            card.add(createTextBlock("دلیل مراجعه", appointment.reason()));
        }

        if (appointment.notes() != null && !appointment.notes().isBlank()) {
            card.add(createTextBlock("یادداشت‌ها", appointment.notes()));
        }

        if (card.getComponentCount() == 0) {
            Span empty = new Span("دلیل یا یادداشتی ثبت نشده است.");
            empty.getStyle().set("color", "#9CA3AF").set("font-size", "13px");
            card.add(empty);
        }

        return card;
    }

    private Component createTextBlock(String label, String text) {
        H3 labelH = new H3(label);
        labelH.getStyle()
                .set("margin", "0 0 6px 0")
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("color", "#374151");

        Paragraph p = new Paragraph(text);
        p.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#4B5563")
                .set("line-height", "1.8");

        VerticalLayout block = new VerticalLayout(labelH, p);
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "4px");
        return block;
    }

    // ============================================
    // دکمه‌های اکشن
    // ============================================
    private Component createActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.getStyle().set("gap", "12px").set("flex-wrap", "wrap");

        if (canCancel()) {
            Button cancel = new Button("لغو نوبت", VaadinIcon.CLOSE_SMALL.create());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);
            cancel.addClickListener(e -> confirmCancel());
            buttons.add(cancel);
        }

        if (appointment.status() == AppointmentStatus.COMPLETED
                || appointment.status() == AppointmentStatus.CANCELLED) {
            Span info = new Span("این نوبت دیگر قابل تغییر نیست.");
            info.getStyle()
                    .set("color", "#6B7280")
                    .set("font-size", "13px")
                    .set("display", "flex")
                    .set("align-items", "center");
            buttons.add(info);
        }

        return buttons;
    }

    private boolean canCancel() {
        AppointmentStatus s = appointment.status();
        if (s == AppointmentStatus.CANCELLED
                || s == AppointmentStatus.COMPLETED
                || s == AppointmentStatus.NO_SHOW) {
            return false;
        }
        return appointment.appointmentTime().isAfter(LocalDateTime.now());
    }

    private void confirmCancel() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("لغو نوبت");
        dialog.setText("آیا از لغو این نوبت مطمئن هستید؟");
        dialog.setCancelable(true);
        dialog.setCancelText("انصراف");
        dialog.setConfirmText("بله، لغو کن");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                appointmentService.cancel(appointment.id());
                Notification.show("نوبت لغو شد", 3000, Notification.Position.TOP_CENTER);
                getUI().ifPresent(ui -> ui.navigate("appointments"));
            } catch (Exception ex) {
                Notification.show("خطا در لغو نوبت: " + ex.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        });
        dialog.open();
    }

    // ============================================
    // حالت «پیدا نشد»
    // ============================================
    private void renderNotFound() {
        removeAll();

        Icon icon = VaadinIcon.FROWN_O.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "#D1D5DB");

        H2 title = new H2("نوبت پیدا نشد");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("color", "#374151");

        Paragraph text = new Paragraph("ممکن است نوبت حذف شده باشد یا شناسه اشتباه باشد.");
        text.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        Button back = new Button("بازگشت به نوبت‌های من", VaadinIcon.ARROW_RIGHT.create());
        back.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        back.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("appointments")));

        VerticalLayout box = new VerticalLayout(icon, title, text, back);
        box.setAlignItems(FlexComponent.Alignment.CENTER);
        box.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        box.setWidthFull();
        box.getStyle()
                .set("padding", "80px 20px")
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("gap", "12px");

        add(box);
    }

    // ============================================
    // badge وضعیت
    // ============================================
    private Span createStatusBadge(AppointmentStatus status) {
        String label;
        String color;
        String bg;
        switch (status) {
            case SCHEDULED -> { label = "برنامه‌ریزی‌شده"; color = "#7C3AED"; bg = "#EDE9FE"; }
            case CONFIRMED -> { label = "تأییدشده"; color = "#059669"; bg = "#D1FAE5"; }
            case COMPLETED -> { label = "انجام‌شده"; color = "#3B82F6"; bg = "#DBEAFE"; }
            case CANCELLED -> { label = "لغوشده"; color = "#DC2626"; bg = "#FEE2E2"; }
            case NO_SHOW  -> { label = "عدم حضور"; color = "#F59E0B"; bg = "#FEF3C7"; }
            default       -> { label = status.name(); color = "#6B7280"; bg = "#F3F4F6"; }
        }
        Span badge = new Span(label);
        badge.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "5px 14px")
                .set("border-radius", "12px")
                .set("color", color)
                .set("background", bg);
        return badge;
    }

    // ============================================
    // کمکی‌ها
    // ============================================
    private static String getInitials(String first, String last) {
        String f = (first != null && !first.isBlank()) ? first.substring(0, 1) : "";
        String l = (last != null && !last.isBlank()) ? last.substring(0, 1) : "";
        String result = (f + l).trim();
        return result.isEmpty() ? "?" : result;
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

    private static String formatFee(BigDecimal fee) {
        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("fa", "IR"));
        return nf.format(fee);
    }
}