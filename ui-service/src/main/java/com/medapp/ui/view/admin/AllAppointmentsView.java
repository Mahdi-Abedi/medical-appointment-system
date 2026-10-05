package com.medapp.ui.view.admin;

import com.medapp.ui.dto.AppointmentResponse;
import com.medapp.ui.dto.AppointmentStatus;
import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.dto.UpdateAppointmentStatusRequest;
import com.medapp.ui.dto.UserResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.AppointmentService;
import com.medapp.ui.service.DoctorService;
import com.medapp.ui.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "admin/appointments", layout = MainLayout.class)
@PageTitle("همه‌ی نوبت‌ها | MedApp")
@RolesAllowed("ADMIN")
public class AllAppointmentsView extends VerticalLayout {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final UserService userService;

    private final Grid<AppointmentResponse> grid = new Grid<>(AppointmentResponse.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<StatusFilter> statusFilter = new ComboBox<>();
    private final ComboBox<TimeFilter> timeFilter = new ComboBox<>();

    private List<AppointmentResponse> allAppointments;
    private final Map<Long, DoctorResponse> doctorCache = new HashMap<>();
    private final Map<Long, UserResponse> userCache = new HashMap<>();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public AllAppointmentsView(AppointmentService appointmentService,
                               DoctorService doctorService,
                               UserService userService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");

        configureGrid();

        add(
                createHeader(),
                createFilterBar(),
                createGridCard()
        );

        loadAppointments();
    }

    // ============================================
    // هدر
    // ============================================
    private Component createHeader() {
        H2 title = new H2("همه‌ی نوبت‌ها");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("مشاهده و مدیریت نوبت‌های کل سیستم");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        VerticalLayout titleBox = new VerticalLayout(title, subtitle);
        titleBox.setPadding(false);
        titleBox.setSpacing(false);
        titleBox.getStyle().set("gap", "2px");

        Button refresh = new Button("بروزرسانی", VaadinIcon.REFRESH.create());
        refresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refresh.addClickListener(e -> loadAppointments());

        Button back = new Button("بازگشت", VaadinIcon.ARROW_RIGHT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("admin")));

        HorizontalLayout actions = new HorizontalLayout(refresh, back);
        actions.setSpacing(true);
        actions.getStyle().set("gap", "8px");

        HorizontalLayout row = new HorizontalLayout(titleBox, actions);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return row;
    }

    // ============================================
    // نوار فیلتر
    // ============================================
    private Component createFilterBar() {
        searchField.setPlaceholder("جستجو نام پزشک یا شناسه کاربر...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> applyFilters());

        statusFilter.setLabel("وضعیت");
        statusFilter.setItems(StatusFilter.values());
        statusFilter.setValue(StatusFilter.ALL);
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        timeFilter.setLabel("زمان");
        timeFilter.setItems(TimeFilter.values());
        timeFilter.setValue(TimeFilter.ALL);
        timeFilter.setWidth("150px");
        timeFilter.addValueChangeListener(e -> applyFilters());

        HorizontalLayout bar = new HorizontalLayout(searchField, statusFilter, timeFilter);
        bar.setAlignItems(FlexComponent.Alignment.END);
        bar.setSpacing(true);
        bar.getStyle()
                .set("gap", "16px")
                .set("flex-wrap", "wrap")
                .set("background", "white")
                .set("padding", "16px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)");
        return bar;
    }

    // ============================================
    // Grid
    // ============================================
    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("550px");

        grid.addColumn(a -> toPersianDigits(String.valueOf(a.id())))
                .setHeader("شناسه").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(a -> {
            DoctorResponse d = doctorCache.get(a.doctorId());
            return d != null ? "دکتر " + d.firstName() + " " + d.lastName()
                    : "پزشک #" + a.doctorId();
        }).setHeader("پزشک").setAutoWidth(true).setFlexGrow(2);

        grid.addColumn(a -> {
            UserResponse u = userCache.get(a.userId());
            return u != null ? u.firstName() + " " + u.lastName()
                    : "کاربر #" + a.userId();
        }).setHeader("کاربر").setAutoWidth(true).setFlexGrow(2);

        grid.addColumn(a -> toPersianDigits(a.appointmentTime().format(DATE_FMT)))
                .setHeader("تاریخ").setAutoWidth(true).setFlexGrow(1);

        grid.addColumn(a -> toPersianDigits(a.appointmentTime().format(TIME_FMT)))
                .setHeader("ساعت").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(a -> a.durationMinutes() != null
                        ? toPersianDigits(a.durationMinutes().toString()) + " د"
                        : "—")
                .setHeader("مدت").setAutoWidth(true).setFlexGrow(0);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("وضعیت").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(a -> a.reason() != null && !a.reason().isBlank()
                        ? (a.reason().length() > 30
                            ? a.reason().substring(0, 30) + "..."
                            : a.reason())
                        : "—")
                .setHeader("دلیل").setAutoWidth(true).setFlexGrow(2);

        grid.addComponentColumn(this::createActions)
                .setHeader("عملیات").setAutoWidth(true).setFlexGrow(0);
    }

    private Component createGridCard() {
        H3 title = new H3("لیست نوبت‌ها");
        title.getStyle()
                .set("margin", "0 0 12px 0")
                .set("font-size", "16px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        VerticalLayout card = new VerticalLayout(title, grid);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("width", "100%");
        return card;
    }

    // ============================================
    // badge وضعیت
    // ============================================
    private Span createStatusBadge(AppointmentResponse a) {
        AppointmentStatus s = a.status();
        String label, color, bg;
        switch (s) {
            case SCHEDULED -> { label = "برنامه‌ریزی‌شده"; color = "#7C3AED"; bg = "#EDE9FE"; }
            case CONFIRMED -> { label = "تأییدشده"; color = "#059669"; bg = "#D1FAE5"; }
            case COMPLETED -> { label = "انجام‌شده"; color = "#3B82F6"; bg = "#DBEAFE"; }
            case CANCELLED -> { label = "لغوشده"; color = "#DC2626"; bg = "#FEE2E2"; }
            case NO_SHOW  -> { label = "عدم حضور"; color = "#F59E0B"; bg = "#FEF3C7"; }
            default       -> { label = s.name(); color = "#6B7280"; bg = "#F3F4F6"; }
        }
        Span badge = new Span(label);
        badge.getStyle()
                .set("font-size", "11px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "10px")
                .set("color", color)
                .set("background", bg)
                .set("white-space", "nowrap");
        return badge;
    }

    // ============================================
    // دکمه‌های عملیات
    // ============================================
    private Component createActions(AppointmentResponse a) {
        Button view = new Button(VaadinIcon.EYE.create());
        view.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        view.setTooltipText("مشاهده جزئیات");
        view.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("appointments/" + a.id())));

        Button changeStatus = new Button(VaadinIcon.EDIT.create());
        changeStatus.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        changeStatus.setTooltipText("تغییر وضعیت");
        changeStatus.addClickListener(e -> openStatusDialog(a));

        HorizontalLayout actions = new HorizontalLayout(view, changeStatus);
        actions.setSpacing(false);
        actions.getStyle().set("gap", "4px");
        return actions;
    }

    // ============================================
    // Dialog تغییر وضعیت
    // ============================================
    private void openStatusDialog(AppointmentResponse appt) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("تغییر وضعیت نوبت #" + toPersianDigits(String.valueOf(appt.id())));
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");

        ComboBox<AppointmentStatus> statusCombo = new ComboBox<>("وضعیت جدید");
        statusCombo.setItems(AppointmentStatus.values());
        statusCombo.setValue(appt.status());
        statusCombo.setWidthFull();
        statusCombo.setRequiredIndicatorVisible(true);

        TextArea notes = new TextArea("یادداشت (اختیاری)");
        notes.setMaxLength(500);
        notes.setWidthFull();
        notes.setHeight("100px");
        notes.setValue(appt.notes() != null ? appt.notes() : "");

        VerticalLayout content = new VerticalLayout(statusCombo, notes);
        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("gap", "12px");

        Button save = new Button("ذخیره");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            if (statusCombo.getValue() == null) {
                Notification.show("لطفاً وضعیت را انتخاب کنید", 3000,
                        Notification.Position.MIDDLE);
                return;
            }
            try {
                appointmentService.updateStatus(
                        appt.id(),
                        statusCombo.getValue(),
                        notes.getValue() != null ? notes.getValue().trim() : null
                );
                Notification.show("وضعیت با موفقیت تغییر یافت", 3000,
                        Notification.Position.TOP_CENTER);
                dialog.close();
                loadAppointments();
            } catch (Exception ex) {
                Notification.show("خطا: " + ex.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        });

        Button cancel = new Button("انصراف", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(content);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    // ============================================
    // بارگذاری
    // ============================================
    private void loadAppointments() {
        allAppointments = appointmentService.getAllAppointments();

        // پر کردن کش پزشکان
        doctorCache.clear();
        for (AppointmentResponse a : allAppointments) {
            if (a.doctorId() != null && !doctorCache.containsKey(a.doctorId())) {
                doctorService.getDoctorById(a.doctorId())
                        .ifPresent(d -> doctorCache.put(a.doctorId(), d));
            }
        }

        // پر کردن کش کاربران
        userCache.clear();
        for (AppointmentResponse a : allAppointments) {
            if (a.userId() != null && !userCache.containsKey(a.userId())) {
                userService.getUserById(a.userId())
                        .ifPresent(u -> userCache.put(a.userId(), u));
            }
        }

        applyFilters();
    }

    // ============================================
    // اعمال فیلترها
    // ============================================
    private void applyFilters() {
        if (allAppointments == null) {
            grid.setItems(List.of());
            return;
        }

        String query = searchField.getValue();
        StatusFilter sf = statusFilter.getValue();
        TimeFilter tf = timeFilter.getValue();
        LocalDateTime now = LocalDateTime.now();

        List<AppointmentResponse> filtered = allAppointments.stream()
                .filter(a -> {
                    if (sf != null && sf != StatusFilter.ALL) {
                        if (a.status() != sf.toEnum()) return false;
                    }
                    if (tf == TimeFilter.UPCOMING) {
                        if (!a.appointmentTime().isAfter(now)) return false;
                    } else if (tf == TimeFilter.PAST) {
                        if (!a.appointmentTime().isBefore(now)) return false;
                    }
                    if (query != null && !query.isBlank()) {
                        String q = query.trim().toLowerCase();
                        DoctorResponse d = doctorCache.get(a.doctorId());
                        UserResponse u = userCache.get(a.userId());
                        String doctorName = d != null
                                ? ("دکتر " + d.firstName() + " " + d.lastName()).toLowerCase()
                                : "";
                        String userName = u != null
                                ? (u.firstName() + " " + u.lastName()).toLowerCase()
                                : "";
                        String userIdStr = String.valueOf(a.userId());
                        if (!doctorName.contains(q)
                                && !userName.contains(q)
                                && !userIdStr.contains(q)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((a, b) -> b.appointmentTime().compareTo(a.appointmentTime()))
                .toList();

        grid.setItems(filtered);
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
    // enum فیلتر وضعیت
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

    // ============================================
    // enum فیلتر زمانی
    // ============================================
    private enum TimeFilter {
        ALL("همه"),
        UPCOMING("آینده"),
        PAST("گذشته");

        private final String label;

        TimeFilter(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}