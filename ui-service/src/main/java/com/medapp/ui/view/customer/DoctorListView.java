package com.medapp.ui.view.customer;

import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.DoctorService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Route(value = "doctors", layout = MainLayout.class)
@PageTitle("پزشکان | MedApp")
@PermitAll
public class DoctorListView extends VerticalLayout {

    private final DoctorService doctorService;

    // کامپوننت‌های فیلتر
    private final TextField searchField = new TextField();
    private final ComboBox<String> specializationFilter = new ComboBox<>();
    private final Checkbox onlyAvailable = new Checkbox("فقط پزشکان موجود");

    // گرید کارت‌ها
    private final VerticalLayout cardsContainer = new VerticalLayout();

    // کش لیست اصلی
    private List<DoctorResponse> allDoctors;

    public DoctorListView(DoctorService doctorService) {
        this.doctorService = doctorService;

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

        // بارگذاری اولیه
        loadDoctors();
    }

    // ============================================
    // هدر صفحه
    // ============================================
    private Component createHeader() {
        H2 title = new H2("پزشکان");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("لیست پزشکان متخصص MedApp");
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
    // نوار فیلتر
    // ============================================
    private Component createFilterBar() {
        // ۱) جستجوی نام
        searchField.setPlaceholder("جستجوی نام پزشک...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("280px");
        searchField.addValueChangeListener(e -> applyFilters());

        // ۲) فیلتر تخصص
        specializationFilter.setPlaceholder("همه‌ی تخصص‌ها");
        specializationFilter.setClearButtonVisible(true);
        specializationFilter.setWidth("220px");
        specializationFilter.addValueChangeListener(e -> applyFilters());

        // ۳) فقط موجودها
        onlyAvailable.addValueChangeListener(e -> applyFilters());
        onlyAvailable.getStyle().set("align-self", "center");

        HorizontalLayout filters = new HorizontalLayout(
                searchField,
                specializationFilter,
                onlyAvailable
        );
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.setSpacing(true);
        filters.getStyle()
                .set("gap", "16px")
                .set("flex-wrap", "wrap")
                .set("background", "white")
                .set("padding", "16px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)");

        return filters;
    }

    // ============================================
    // بارگذاری لیست پزشکان
    // ============================================
    private void loadDoctors() {
        allDoctors = doctorService.getAllDoctors();

        // پر کردن فیلتر تخصص
        List<String> specializations = allDoctors.stream()
                .map(DoctorResponse::specialization)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();
        specializationFilter.setItems(specializations);

        applyFilters();
    }

    // ============================================
    // اعمال فیلترها
    // ============================================
    private void applyFilters() {
        cardsContainer.removeAll();

        if (allDoctors == null || allDoctors.isEmpty()) {
            cardsContainer.add(createEmptyState("هنوز پزشکی ثبت نشده است."));
            return;
        }

        String query = searchField.getValue();
        String spec = specializationFilter.getValue();
        boolean availableOnly = onlyAvailable.getValue();

        List<DoctorResponse> filtered = allDoctors.stream()
                .filter(d -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.trim().toLowerCase();
                        String fullName = (d.firstName() + " " + d.lastName()).toLowerCase();
                        if (!fullName.contains(q)) return false;
                    }
                    if (spec != null && !spec.isBlank()) {
                        if (!spec.equals(d.specialization())) return false;
                    }
                    if (availableOnly && !d.available()) return false;
                    return true;
                })
                .toList();

        if (filtered.isEmpty()) {
            cardsContainer.add(createEmptyState("پزشکی با این مشخصات پیدا نشد."));
            return;
        }

        // گرید کارت‌ها
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle()
                .set("gap", "16px")
                .set("flex-wrap", "wrap");

        for (DoctorResponse doctor : filtered) {
            row.add(createDoctorCard(doctor));
        }

        cardsContainer.add(row);
        cardsContainer.setPadding(false);
        cardsContainer.setSpacing(false);
    }

    // ============================================
    // کارت پزشک
    // ============================================
    private Component createDoctorCard(DoctorResponse doctor) {
        // آواتار با حرف اول نام
        String initials = getInitials(doctor.firstName(), doctor.lastName());
        Span avatar = new Span(initials);
        avatar.getStyle()
                .set("width", "56px")
                .set("height", "56px")
                .set("border-radius", "50%")
                .set("background", "linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)")
                .set("color", "white")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("flex-shrink", "0");

        // نام کامل
        H3 name = new H3("دکتر " + doctor.firstName() + " " + doctor.lastName());
        name.getStyle()
                .set("margin", "0")
                .set("font-size", "17px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        // تخصص
        Span spec = new Span(doctor.specialization() != null ? doctor.specialization() : "—");
        spec.getStyle()
                .set("font-size", "13px")
                .set("color", "#7C3AED")
                .set("font-weight", "600");

        // سابقه
        Span experience = new Span();
        if (doctor.yearsOfExperience() != null) {
            experience.setText("سابقه: " + toPersianDigits(doctor.yearsOfExperience().toString()) + " سال");
        } else {
            experience.setText("سابقه: —");
        }
        experience.getStyle()
                .set("font-size", "12px")
                .set("color", "#6B7280");

        // هزینه ویزیت
        Span fee = new Span();
        if (doctor.consultationFee() != null) {
            fee.setText("ویزیت: " + formatFee(doctor.consultationFee()) + " تومان");
        } else {
            fee.setText("ویزیت: —");
        }
        fee.getStyle()
                .set("font-size", "12px")
                .set("color", "#6B7280");

        VerticalLayout textCol = new VerticalLayout(name, spec, experience, fee);
        textCol.setPadding(false);
        textCol.setSpacing(false);
        textCol.getStyle().set("gap", "4px").set("flex", "1");

        HorizontalLayout mainRow = new HorizontalLayout(avatar, textCol);
        mainRow.setAlignItems(FlexComponent.Alignment.START);
        mainRow.setSpacing(true);
        mainRow.setWidthFull();

        // نشان availability
        Span badge = new Span(doctor.available() ? "موجود" : "غیرفعال");
        badge.getStyle()
                .set("font-size", "11px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("color", doctor.available() ? "#059669" : "#DC2626")
                .set("background", doctor.available() ? "#D1FAE5" : "#FEE2E2");

        HorizontalLayout badgeRow = new HorizontalLayout(badge);
        badgeRow.setWidthFull();
        badgeRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // کارت
        VerticalLayout card = new VerticalLayout(mainRow, badgeRow);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("width", "320px")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease")
                .set("gap", "8px")
                .set("border", "1px solid transparent");

        // hover effect
        card.getElement().addEventListener("mouseenter", e ->
                card.getStyle()
                        .set("box-shadow", "0 4px 12px rgba(124, 58, 237, 0.15)")
                        .set("border-color", "#C4B5FD"));
        card.getElement().addEventListener("mouseleave", e ->
                card.getStyle()
                        .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                        .set("border-color", "transparent"));

        // کلیک → صفحه جزئیات
        card.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("doctors/" + doctor.id())));

        return card;
    }

    // ============================================
    // حالت خالی
    // ============================================
    private Component createEmptyState(String message) {
        Icon icon = VaadinIcon.SEARCH.create();
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