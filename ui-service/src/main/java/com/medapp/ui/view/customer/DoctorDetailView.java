package com.medapp.ui.view.customer;

import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.DoctorService;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

@Route(value = "doctors/:doctorId", layout = MainLayout.class)
@PageTitle("جزئیات پزشک | MedApp")
@PermitAll
public class DoctorDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final DoctorService doctorService;

    private Long doctorId;
    private DoctorResponse doctor;

    public DoctorDetailView(DoctorService doctorService) {
        this.doctorService = doctorService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");
    }

    // ============================================
    // گرفتن ID از URL قبل از رندر
    // ============================================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("doctorId").orElse(null);
        if (idParam == null) {
            event.rerouteTo(DoctorListView.class);
            return;
        }
        try {
            this.doctorId = Long.parseLong(idParam);
        } catch (NumberFormatException ex) {
            event.rerouteTo(DoctorListView.class);
            return;
        }

        // واکشی پزشک
        Optional<DoctorResponse> result = doctorService.getDoctorById(doctorId);
        if (result.isEmpty()) {
            renderNotFound();
            return;
        }
        this.doctor = result.get();
        renderContent();
    }

    // ============================================
    // محتوای اصلی
    // ============================================
    private void renderContent() {
        removeAll();

        H2 pageTitle = new H2("جزئیات پزشک");
        pageTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        add(
                createBackButton(),
                pageTitle,
                createProfileCard(),
                createInfoCards(),
                createActionButtons()
        );
    }

    // ============================================
    // دکمه بازگشت
    // ============================================
    private Component createBackButton() {
        Button back = new Button("بازگشت به لیست پزشکان", VaadinIcon.ARROW_RIGHT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("margin-bottom", "8px");
        back.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("doctors")));
        return back;
    }

    // ============================================
    // کارت پروفایل (آواتار + نام + تخصص + badge)
    // ============================================
    private Component createProfileCard() {
        String initials = getInitials(doctor.firstName(), doctor.lastName());
        Span avatar = new Span(initials);
        avatar.getStyle()
                .set("width", "96px")
                .set("height", "96px")
                .set("border-radius", "50%")
                .set("background", "linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)")
                .set("color", "white")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "36px")
                .set("font-weight", "700")
                .set("flex-shrink", "0");

        H3 name = new H3("دکتر " + doctor.firstName() + " " + doctor.lastName());
        name.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Span spec = new Span(doctor.specialization() != null ? doctor.specialization() : "—");
        spec.getStyle()
                .set("font-size", "15px")
                .set("color", "#7C3AED")
                .set("font-weight", "600");

        Span badge = new Span(doctor.available() ? "موجود" : "غیرفعال");
        badge.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("color", doctor.available() ? "#059669" : "#DC2626")
                .set("background", doctor.available() ? "#D1FAE5" : "#FEE2E2")
                .set("display", "inline-block");

        VerticalLayout textCol = new VerticalLayout(name, spec, badge);
        textCol.setPadding(false);
        textCol.setSpacing(false);
        textCol.getStyle().set("gap", "6px");

        HorizontalLayout content = new HorizontalLayout(avatar, textCol);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.getStyle().set("gap", "24px");

        VerticalLayout card = new VerticalLayout(content);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "28px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)");
        return card;
    }

    // ============================================
    // کارت‌های اطلاعات (سابقه، ویزیت، ایمیل، تلفن)
    // ============================================
    private Component createInfoCards() {
        HorizontalLayout row = new HorizontalLayout(
                createInfoCard("سابقه‌ی کار",
                        doctor.yearsOfExperience() != null
                                ? toPersianDigits(doctor.yearsOfExperience().toString()) + " سال"
                                : "—",
                        VaadinIcon.TROPHY, "#F59E0B"),
                createInfoCard("هزینه‌ی ویزیت",
                        doctor.consultationFee() != null
                                ? formatFee(doctor.consultationFee()) + " تومان"
                                : "—",
                        VaadinIcon.MONEY, "#10B981"),
                createInfoCard("ایمیل",
                        doctor.email() != null ? doctor.email() : "—",
                        VaadinIcon.ENVELOPE, "#7C3AED"),
                createInfoCard("تلفن",
                        doctor.phone() != null ? doctor.phone() : "—",
                        VaadinIcon.PHONE, "#3B82F6")
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("gap", "16px").set("flex-wrap", "wrap");
        return row;
    }

    private Component createInfoCard(String title, String value, VaadinIcon icon, String color) {
        Icon iconComp = icon.create();
        iconComp.setSize("22px");
        iconComp.getStyle().set("color", color);

        HorizontalLayout iconWrap = new HorizontalLayout(iconComp);
        iconWrap.setAlignItems(FlexComponent.Alignment.CENTER);
        iconWrap.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        iconWrap.getStyle()
                .set("width", "44px")
                .set("height", "44px")
                .set("border-radius", "10px")
                .set("background", color + "20")
                .set("flex-shrink", "0");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#6B7280");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("color", "#1F2937")
                .set("word-break", "break-all");

        VerticalLayout textCol = new VerticalLayout(titleSpan, valueSpan);
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
                .set("padding", "16px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("flex", "1")
                .set("min-width", "220px");
        return card;
    }

    // ============================================
    // Bio (بیوگرافی)
    // ============================================
    private Component createBioCard() {
        if (doctor.bio() == null || doctor.bio().isBlank()) {
            return new Span();
        }
        H3 title = new H3("درباره‌ی پزشک");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "16px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        Paragraph bio = new Paragraph(doctor.bio());
        bio.getStyle()
                .set("margin", "0")
                .set("color", "#4B5563")
                .set("font-size", "14px")
                .set("line-height", "1.8");

        VerticalLayout card = new VerticalLayout(title, bio);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)");
        return card;
    }

    // ============================================
    // دکمه‌های اکشن
    // ============================================
    private Component createActionButtons() {
        Button book = new Button("رزرو نوبت", VaadinIcon.CALENDAR_USER.create());
        book.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        book.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("appointments/new?doctorId=" + doctor.id())));

        Button contact = new Button("تماس با پزشک", VaadinIcon.PHONE.create());
        contact.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        contact.setEnabled(false);
        contact.setTooltipText("به زودی فعال می‌شود");

        HorizontalLayout buttons = new HorizontalLayout(book, contact);
        buttons.setSpacing(true);
        buttons.getStyle().set("gap", "12px").set("flex-wrap", "wrap");

        VerticalLayout wrapper = new VerticalLayout(buttons);
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.getStyle().set("margin-top", "8px");
        return wrapper;
    }

    // ============================================
    // حالت «پیدا نشد»
    // ============================================
    private void renderNotFound() {
        removeAll();

        Icon icon = VaadinIcon.FROWN_O.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "#D1D5DB");

        H2 title = new H2("پزشک پیدا نشد");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("color", "#374151");

        Paragraph text = new Paragraph("ممکن است پزشک حذف شده باشد یا شناسه اشتباه باشد.");
        text.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        Button back = new Button("بازگشت به لیست پزشکان", VaadinIcon.ARROW_RIGHT.create());
        back.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        back.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("doctors")));

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