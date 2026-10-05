package com.medapp.ui.view.customer;

import com.medapp.ui.dto.AppointmentResponse;
import com.medapp.ui.dto.CreateAppointmentRequest;
import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.dto.UserResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.AppointmentService;
import com.medapp.ui.service.DoctorService;
import com.medapp.ui.service.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Route(value = "appointments/new", layout = MainLayout.class)
@PageTitle("رزرو نوبت | MedApp")
@PermitAll
public class BookAppointmentView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    private Long preselectedDoctorId;
    private DoctorResponse selectedDoctor;
    private UserResponse currentUser;

    // فرم‌ها
    private final ComboBox<DoctorResponse> doctorCombo = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker();
    private final TimePicker timePicker = new TimePicker();
    private final ComboBox<Integer> durationCombo = new ComboBox<>();
    private final TextArea reasonArea = new TextArea();

    private final VerticalLayout selectedDoctorCard = new VerticalLayout();

    public BookAppointmentView(SessionService sessionService,
                               DoctorService doctorService,
                               AppointmentService appointmentService) {
        this.sessionService = sessionService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");

        this.currentUser = sessionService.getCurrentUser();

        configureDatePicker();
        configureTimePicker();
        configureDurationCombo();
        configureReasonArea();
        configureDoctorCombo();

        add(
                createHeader(),
                createFormCard(),
                createActionButtons()
        );
    }

    // ============================================
    // گرفتن query parameter ?doctorId=X
    // ============================================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String doctorIdParam = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("doctorId", List.of())
                .stream().findFirst().orElse(null);

        if (doctorIdParam != null) {
            try {
                preselectedDoctorId = Long.parseLong(doctorIdParam);
                Optional<DoctorResponse> doc = doctorService.getDoctorById(preselectedDoctorId);
                doc.ifPresent(d -> {
                    this.selectedDoctor = d;
                    renderSelectedDoctorCard();
                });
            } catch (NumberFormatException ignored) {
            }
        }
    }

    // ============================================
    // کانفیگ DatePicker (شمسی با locale فارسی)
    // ============================================
    private void configureDatePicker() {
        // استفاده از locale فارسی — Vaadin به‌طور پیش‌فرض از تقویم میلادی استفاده می‌کند،
        // اما با locale fa-IR می‌توانید نام ماه‌ها و روزها را فارسی کنید.
        // ⚠️ برای تقویم کاملاً شمسی، نیاز به استفاده از یک کامپوننت community یا
        // تبدیل تاریخ در لایه سرویس دارید. در ادامه توضیح داده شده است.
        // Persian language but Latin digits (u-nu-latn extension)
        timePicker.setLocale(Locale.US);
        datePicker.setLabel("تاریخ نوبت");
        datePicker.setRequiredIndicatorVisible(true);
        datePicker.setMin(LocalDate.now(ZoneId.of("Asia/Tehran")));
        datePicker.setWidthFull();    }

    // ============================================
    // کانفیگ TimePicker (۲۴ ساعته، گام ۳۰ دقیقه)
    // ============================================
    private void configureTimePicker() {
        datePicker.setLocale(Locale.US);
        timePicker.setLabel("ساعت نوبت");
        timePicker.setRequiredIndicatorVisible(true);
        timePicker.setStep(Duration.ofMinutes(30));
        timePicker.setMin(LocalTime.of(8, 0));
        timePicker.setMax(LocalTime.of(20, 0));
        timePicker.setWidthFull();
    }

    // ============================================
    // کانفیگ ComboBox مدت
    // ============================================
    private void configureDurationCombo() {
        durationCombo.setLabel("مدت نوبت (دقیقه)");
        durationCombo.setItems(15, 20, 30, 45, 60);
        durationCombo.setValue(30);
        durationCombo.setRequiredIndicatorVisible(true);
        durationCombo.setWidthFull();
    }

    // ============================================
    // کانفیگ TextArea دلیل
    // ============================================
    private void configureReasonArea() {
        reasonArea.setLabel("دلیل مراجعه");
        reasonArea.setPlaceholder("مثلاً: درد قفسه سینه، معاینه دوره‌ای و ...");
        reasonArea.setMaxLength(500);
        reasonArea.setWidthFull();
        reasonArea.setHeight("100px");
    }

    // ============================================
    // کانفیگ ComboBox پزشک
    // ============================================
    private void configureDoctorCombo() {
        doctorCombo.setLabel("پزشک");
        doctorCombo.setPlaceholder("یک پزشک انتخاب کنید");
        doctorCombo.setRequiredIndicatorVisible(true);
        doctorCombo.setWidthFull();

        List<DoctorResponse> doctors = doctorService.getAllDoctors();
        doctorCombo.setItems(doctors);
        doctorCombo.setItemLabelGenerator(d ->
                "دکتر " + d.firstName() + " " + d.lastName()
                        + " - " + (d.specialization() != null ? d.specialization() : "—"));

        doctorCombo.addValueChangeListener(e -> {
            selectedDoctor = e.getValue();
            renderSelectedDoctorCard();
        });
    }

    // ============================================
    // هدر
    // ============================================
    private Component createHeader() {
        H2 title = new H2("رزرو نوبت جدید");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("لطفاً اطلاعات نوبت را وارد کنید");
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

    // ============================================
    // کارت فرم
    // ============================================
    private Component createFormCard() {
        H3 formTitle = new H3("اطلاعات نوبت");
        formTitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "16px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        // اگر پزشک از قبل انتخاب شده باشد، فقط نمایش می‌دهیم
        if (selectedDoctor != null) {
            doctorCombo.setVisible(false);
            selectedDoctorCard.setVisible(true);
            renderSelectedDoctorCard();
        } else {
            selectedDoctorCard.setVisible(false);
        }

        VerticalLayout form = new VerticalLayout(
                doctorCombo,
                selectedDoctorCard,
                datePicker,
                timePicker,
                durationCombo,
                reasonArea
        );
        form.setPadding(false);
        form.setSpacing(true);
        form.getStyle().set("gap", "16px");

        VerticalLayout card = new VerticalLayout(formTitle, form);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "28px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("max-width", "600px");

        return card;
    }

    // ============================================
    // کارت نمایش پزشک انتخاب‌شده
    // ============================================
    private void renderSelectedDoctorCard() {
        selectedDoctorCard.removeAll();

        if (selectedDoctor == null) {
            selectedDoctorCard.setVisible(false);
            return;
        }

        String initials = getInitials(selectedDoctor.firstName(), selectedDoctor.lastName());
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

        Span name = new Span("دکتر " + selectedDoctor.firstName() + " " + selectedDoctor.lastName());
        name.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        Span spec = new Span(selectedDoctor.specialization() != null ? selectedDoctor.specialization() : "—");
        spec.getStyle()
                .set("font-size", "13px")
                .set("color", "#7C3AED");

        VerticalLayout textCol = new VerticalLayout(name, spec);
        textCol.setPadding(false);
        textCol.setSpacing(false);
        textCol.getStyle().set("gap", "2px");

        HorizontalLayout content = new HorizontalLayout(avatar, textCol);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.getStyle().set("gap", "12px");

        selectedDoctorCard.add(content);
        selectedDoctorCard.setPadding(true);
        selectedDoctorCard.setSpacing(false);
        selectedDoctorCard.getStyle()
                .set("background", "#F5F3FF")
                .set("border-radius", "12px")
                .set("padding", "16px")
                .set("border", "1px solid #E0D5FF")
                .set("width", "100%");
        selectedDoctorCard.setVisible(true);
    }

    // ============================================
    // دکمه‌های اکشن
    // ============================================
    private Component createActionButtons() {
        Button submit = new Button("ثبت نوبت", VaadinIcon.CALENDAR_USER.create());
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submit.addClickListener(e -> submitAppointment());

        Button cancel = new Button("انصراف");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        cancel.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("doctors")));

        HorizontalLayout buttons = new HorizontalLayout(submit, cancel);
        buttons.setSpacing(true);
        buttons.getStyle().set("gap", "12px").set("flex-wrap", "wrap");
        return buttons;
    }

    // ============================================
    // ثبت نوبت
    // ============================================
    private void submitAppointment() {
        // اعتبارسنجی
        if (selectedDoctor == null) {
            Notification.show("لطفاً یک پزشک انتخاب کنید", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (datePicker.getValue() == null) {
            Notification.show("لطفاً تاریخ نوبت را انتخاب کنید", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (timePicker.getValue() == null) {
            Notification.show("لطفاً ساعت نوبت را انتخاب کنید", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (currentUser == null || currentUser.id() == null) {
            Notification.show("اطلاعات کاربر یافت نشد. لطفاً دوباره وارد شوید.", 3000, Notification.Position.MIDDLE);
            return;
        }

        LocalDateTime appointmentTime = LocalDateTime.of(
                datePicker.getValue(),
                timePicker.getValue()
        );

        if (appointmentTime.isBefore(LocalDateTime.now())) {
            Notification.show("زمان نوبت باید در آینده باشد", 3000, Notification.Position.MIDDLE);
            return;
        }

        Integer duration = durationCombo.getValue() != null ? durationCombo.getValue() : 30;

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                currentUser.id(),
                selectedDoctor.id(),
                appointmentTime,
                duration,
                reasonArea.getValue()
        );

        try {
            AppointmentResponse created = appointmentService.createAppointment(request);
            Notification.show("نوبت با موفقیت ثبت شد!", 3000, Notification.Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate("appointments"));
        } catch (Exception ex) {
            Notification.show("خطا در ثبت نوبت: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
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
}