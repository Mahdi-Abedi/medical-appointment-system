package com.medapp.ui.view.customer;

import com.medapp.ui.dto.AppointmentResponse;
import com.medapp.ui.dto.AppointmentStatus;
import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.dto.UserResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.AppointmentService;
import com.medapp.ui.service.DoctorService;
import com.medapp.ui.service.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "appointments", layout = MainLayout.class)
@PageTitle("نوبت‌های من | MedApp")
@PermitAll
public class MyAppointmentsView extends VerticalLayout {

    private final SessionService sessionService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    private UserResponse currentUser;
    private List<AppointmentResponse> allAppointments;
    private Map<Long, DoctorResponse> doctorCache = new HashMap<>();

    // کامپوننت‌ها
    private final ComboBox<StatusFilter> statusFilter = new ComboBox<>();
    private final Tabs timeTabs = new Tabs();
    private final VerticalLayout cardsContainer = new VerticalLayout();

    // فرمترها
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public MyAppointmentsView(SessionService sessionService,
                              AppointmentService appointmentService,
                              DoctorService doctorService) {
        this.sessionService = sessionService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;

        this.currentUser = sessionService.getCurrentUser();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");

        add(
                createHeader(),
                createFilterBar(),
                cardsContainer
        );

        loadAppointments();
    }

    // ============================================
    // هدر
    // ============================================
    private Component createHeader() {
        H2 title = new H2("نوبت‌های من");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("مدیریت و مشاهده‌ی نوبت‌های شما");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        Button newAppointment = new Button("رزرو نوبت جدید", VaadinIcon.PLUS.create());
        newAppointment.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newAppointment.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("appointments/new")));

        HorizontalLayout titleRow = new HorizontalLayout(
                new VerticalLayout(title, subtitle),
                newAppointment
        );
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.getChildren()
                .findFirst()
                .ifPresent(c -> ((VerticalLayout) c).setPadding(false));
        titleRow.getChildren()
                .findFirst()
                .ifPresent(c -> ((VerticalLayout) c).setSpacing(false));
        return titleRow;
    }

    // ============================================
    // نوار فیلتر (وضعیت + تب زمانی)
    // ============================================
    private Component createFilterBar() {
        statusFilter.setLabel("وضعیت");
        statusFilter.setItems(StatusFilter.values());
        statusFilter.setValue(StatusFilter.ALL);
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        // تب‌های زمانی
        Tab allTab = new Tab("همه");
        Tab upcomingTab = new Tab("آینده");
        Tab pastTab = new Tab("گذشته");
        timeTabs.add(allTab, upcomingTab, pastTab);
        timeTabs.setSelectedTab(allTab);
        timeTabs.addSelectedChangeListener(e -> applyFilters());

        HorizontalLayout filters = new HorizontalLayout(timeTabs, statusFilter);
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        filters.getStyle()
                .set("background", "white")
                .set("padding", "12px 16px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("flex-wrap", "wrap")
                .set("gap", "16px");
        return filters;
    }

    // ============================================
    // بارگذاری نوبت‌ها
    // ============================================
    private void loadAppointments() {
        if (currentUser == null || currentUser.id() == null) {
            cardsContainer.removeAll();
            cardsContainer.add(createEmptyState("لطفاً دوباره وارد شوید."));
            return;
        }

        allAppointments = appointmentService.getMyAppointments(currentUser.id());
        doctorCache.clear();

        // پر کردن کش پزشکان
        for (AppointmentResponse a : allAppointments) {
            if (a.doctorId() != null && !doctorCache.containsKey(a.doctorId())) {
                doctorService.getDoctorById(a.doctorId())
                        .ifPresent(d -> doctorCache.put(a.doctorId(), d));
            }
        }

        applyFilters();
    }

    // ============================================
    // اعمال فیلترها
    // ============================================
    private void applyFilters() {
        cardsContainer.removeAll();

        if (allAppointments == null || allAppointments.isEmpty()) {
            cardsContainer.add(createEmptyState(
                    "هنوز نوبتی ثبت نکرده‌اید. یک نوبت جدید رزرو کنید!"));
            return;
        }

        StatusFilter status = statusFilter.getValue();
        String timeFilter = timeTabs.getSelectedTab() != null
                ? timeTabs.getSelectedTab().getLabel()
                : "همه";

        LocalDateTime now = LocalDateTime.now();

        List<AppointmentResponse> filtered = allAppointments.stream()
                .filter(a -> {
                    // فیلتر وضعیت
                    if (status != StatusFilter.ALL && a.status() != status.toEnum()) {
                        return false;
                    }
                    // فیلتر زمانی
                    if ("آینده".equals(timeFilter)) {
                        return a.appointmentTime().isAfter(now)
                                && a.status() != AppointmentStatus.CANCELLED
                                && a.status() != AppointmentStatus.COMPLETED;
                    }
                    if ("گذشته".equals(timeFilter)) {
                        return a.appointmentTime().isBefore(now)
                                || a.status() == AppointmentStatus.CANCELLED
                                || a.status() == AppointmentStatus.COMPLETED;
                    }
                    return true;
                })
                .sorted((a, b) -> b.appointmentTime().compareTo(a.appointmentTime()))
                .toList();

        if (filtered.isEmpty()) {
            cardsContainer.add(createEmptyState("نوبتی با این فیلتر پیدا نشد."));
            return;
        }

        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(true);
        list.getStyle().set("gap", "12px");
        for (AppointmentResponse a : filtered) {
            list.add(createAppointmentCard(a));
        }
        cardsContainer.add(list);
        cardsContainer.setPadding(false);
        cardsContainer.setSpacing(false);
    }

    // ============================================
    // کارت نوبت
    // ============================================
    private Component createAppointmentCard(AppointmentResponse appt) {
        DoctorResponse doctor = doctorCache.get(appt.doctorId());
        String doctorName = doctor != null
                ? "دکتر " + doctor.firstName() + " " + doctor.lastName()
                : "پزشک #" + appt.doctorId();
        String specialization = doctor != null && doctor.specialization() != null
                ? doctor.specialization() : "—";

        // ستون چپ: تاریخ و ساعت (بزرگ)
        Span dateSpan = new Span(toPersianDigits(appt.appointmentTime().format(DATE_FMT)));
        dateSpan.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Span timeSpan = new Span(toPersianDigits(appt.appointmentTime().format(TIME_FMT)));
        timeSpan.getStyle()
                .set("font-size", "15px")
                .set("color", "#7C3AED")
                .set("font-weight", "600");

        VerticalLayout dateCol = new VerticalLayout(dateSpan, timeSpan);
        dateCol.setPadding(false);
        dateCol.setSpacing(false);
        dateCol.setAlignItems(FlexComponent.Alignment.CENTER);
        dateCol.getStyle()
                .set("background", "#F5F3FF")
                .set("border-radius", "10px")
                .set("padding", "12px 16px")
                .set("gap", "2px")
                .set("min-width", "100px");

        // ستون میانی: نام پزشک + تخصص + مدت
        H3 nameH = new H3(doctorName);
        nameH.getStyle()
                .set("margin", "0")
                .set("font-size", "16px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        Span specSpan = new Span(specialization);
        specSpan.getStyle()
                .set("font-size", "13px")
                .set("color", "#7C3AED")
                .set("font-weight", "500");

        String duration = appt.durationMinutes() != null
                ? toPersianDigits(appt.durationMinutes().toString()) + " دقیقه"
                : "—";
        Span durationSpan = new Span("مدت: " + duration);
        durationSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#6B7280");

        VerticalLayout infoCol = new VerticalLayout(nameH, specSpan, durationSpan);
        infoCol.setPadding(false);
        infoCol.setSpacing(false);
        infoCol.getStyle().set("gap", "4px").set("flex", "1");

        // ستون راست: badge وضعیت + دکمه‌ها
        Span badge = createStatusBadge(appt.status());

        Button detailBtn = new Button("جزئیات", VaadinIcon.EYE.create());
        detailBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        detailBtn.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("appointments/" + appt.id())));

        HorizontalLayout actions = new HorizontalLayout(badge, detailBtn);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setSpacing(true);
        actions.getStyle().set("gap", "8px");

        // دکمه لغو (اگر نوبت قابل لغو باشد)
        if (canCancel(appt)) {
            Button cancelBtn = new Button("لغو", VaadinIcon.CLOSE_SMALL.create());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            cancelBtn.addClickListener(e -> confirmCancel(appt));
            actions.add(cancelBtn);
        }

        // ترکیب
        HorizontalLayout main = new HorizontalLayout(dateCol, infoCol, actions);
        main.setWidthFull();
        main.setAlignItems(FlexComponent.Alignment.CENTER);
        main.setSpacing(true);
        main.getStyle().set("gap", "16px");

        VerticalLayout card = new VerticalLayout(main);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "16px 20px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("border", "1px solid transparent");

        // hover effect
        card.getElement().addEventListener("mouseenter", e ->
                card.getStyle()
                        .set("box-shadow", "0 4px 12px rgba(124, 58, 237, 0.1)")
                        .set("border-color", "#E9D5FF"));
        card.getElement().addEventListener("mouseleave", e ->
                card.getStyle()
                        .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                        .set("border-color", "transparent"));

        return card;
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
                .set("font-size", "11px")
                .set("font-weight", "600")
                .set("padding", "4px 10px")
                .set("border-radius", "10px")
                .set("color", color)
                .set("background", bg)
                .set("white-space", "nowrap");
        return badge;
    }

    // ============================================
    // آیا قابل لغو است؟
    // ============================================
    private boolean canCancel(AppointmentResponse appt) {
        if (appt.status() == AppointmentStatus.CANCELLED
                || appt.status() == AppointmentStatus.COMPLETED
                || appt.status() == AppointmentStatus.NO_SHOW) {
            return false;
        }
        // فقط اگر در آینده است
        return appt.appointmentTime().isAfter(LocalDateTime.now());
    }

    // ============================================
    // تأیید لغو + لغو
    // ============================================
    private void confirmCancel(AppointmentResponse appt) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("لغو نوبت");
        dialog.setText("آیا از لغو این نوبت مطمئن هستید؟ این عمل قابل بازگشت نیست.");
        dialog.setCancelable(true);
        dialog.setCancelText("انصراف");
        dialog.setConfirmText("بله، لغو کن");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                appointmentService.cancel(appt.id());
                Notification.show("نوبت با موفقیت لغو شد", 3000,
                        Notification.Position.TOP_CENTER);
                loadAppointments();
            } catch (Exception ex) {
                Notification.show("خطا در لغو نوبت: " + ex.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        });
        dialog.open();
    }

    // ============================================
    // حالت خالی
    // ============================================
    private Component createEmptyState(String message) {
        Icon icon = VaadinIcon.CALENDAR.create();
        icon.setSize("48px");
        icon.getStyle().set("color", "#D1D5DB");

        Paragraph text = new Paragraph(message);
        text.getStyle()
                .set("color", "#6B7280")
                .set("font-size", "14px")
                .set("margin", "0");

        VerticalLayout empty = new VerticalLayout(icon, text);
        empty.setAlignItems(FlexComponent.Alignment.CENTER);
        empty.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        empty.setWidthFull();
        empty.getStyle()
                .set("padding", "60px 20px")
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)");
        return empty;
    }

    // ============================================
    // کمکی‌ها
    // ============================================
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

    // ============================================
    // Enum فیلتر وضعیت (برای ComboBox)
    // ============================================
    private enum StatusFilter {
        ALL("همه", null),
        SCHEDULED("برنامه‌ریزی‌شده", AppointmentStatus.SCHEDULED),
        CONFIRMED("تأییدشده", AppointmentStatus.CONFIRMED),
        COMPLETED("انجام‌شده", AppointmentStatus.COMPLETED),
        CANCELLED("لغوشده", AppointmentStatus.CANCELLED),
        NO_SHOW("عدم حضور", AppointmentStatus.NO_SHOW);

        private final String label;
        private final AppointmentStatus enumValue;

        StatusFilter(String label, AppointmentStatus enumValue) {
            this.label = label;
            this.enumValue = enumValue;
        }

        public AppointmentStatus toEnum() {
            return enumValue;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}