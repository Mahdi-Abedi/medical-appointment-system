package com.medapp.ui.view.admin;

import com.medapp.ui.dto.CreateDoctorRequest;
import com.medapp.ui.dto.DoctorResponse;
import com.medapp.ui.dto.UpdateDoctorRequest;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.DoctorService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Route(value = "admin/doctors", layout = MainLayout.class)
@PageTitle("مدیریت پزشکان | MedApp")
@RolesAllowed("ADMIN")
public class ManageDoctorsView extends VerticalLayout {

    private final DoctorService doctorService;

    private final Grid<DoctorResponse> grid = new Grid<>(DoctorResponse.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<String> specFilter = new ComboBox<>();
    private final Checkbox availableOnly = new Checkbox("فقط موجودها");

    private List<DoctorResponse> allDoctors;

    public ManageDoctorsView(DoctorService doctorService) {
        this.doctorService = doctorService;

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

        loadDoctors();
    }

    // ============================================
    // هدر + دکمه افزودن
    // ============================================
    private Component createHeader() {
        H2 title = new H2("مدیریت پزشکان");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("افزودن، ویرایش و حذف پزشکان");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        VerticalLayout titleBox = new VerticalLayout(title, subtitle);
        titleBox.setPadding(false);
        titleBox.setSpacing(false);
        titleBox.getStyle().set("gap", "2px");

        Button addBtn = new Button("افزودن پزشک", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        addBtn.addClickListener(e -> openDoctorDialog(null));

        Button back = new Button("بازگشت", VaadinIcon.ARROW_RIGHT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("admin")));

        HorizontalLayout actions = new HorizontalLayout(addBtn, back);
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
        searchField.setPlaceholder("جستجوی نام پزشک...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("280px");
        searchField.addValueChangeListener(e -> applyFilters());

        specFilter.setPlaceholder("همه‌ی تخصص‌ها");
        specFilter.setClearButtonVisible(true);
        specFilter.setWidth("220px");
        specFilter.addValueChangeListener(e -> applyFilters());

        availableOnly.addValueChangeListener(e -> applyFilters());
        availableOnly.getStyle().set("align-self", "center");

        HorizontalLayout bar = new HorizontalLayout(searchField, specFilter, availableOnly);
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
        grid.setHeight("500px");

        grid.addColumn(d -> toPersianDigits(String.valueOf(d.id())))
                .setHeader("شناسه").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(d -> "دکتر " + d.firstName() + " " + d.lastName())
                .setHeader("نام").setAutoWidth(true).setFlexGrow(2);

        grid.addColumn(d -> d.specialization() != null ? d.specialization() : "—")
                .setHeader("تخصص").setAutoWidth(true).setFlexGrow(1);

        grid.addColumn(d -> d.email() != null ? d.email() : "—")
                .setHeader("ایمیل").setAutoWidth(true).setFlexGrow(2);

        grid.addColumn(d -> d.phone() != null ? toPersianDigits(d.phone()) : "—")
                .setHeader("تلفن").setAutoWidth(true).setFlexGrow(1);

        grid.addColumn(d -> d.yearsOfExperience() != null
                        ? toPersianDigits(d.yearsOfExperience().toString()) + " سال"
                        : "—")
                .setHeader("سابقه").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(d -> d.consultationFee() != null
                        ? formatFee(d.consultationFee())
                        : "—")
                .setHeader("ویزیت (تومان)").setAutoWidth(true).setFlexGrow(1);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("وضعیت").setAutoWidth(true).setFlexGrow(0);

        grid.addComponentColumn(this::createActions)
                .setHeader("عملیات").setAutoWidth(true).setFlexGrow(0);
    }

    private Component createGridCard() {
        H3 title = new H3("لیست پزشکان");
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

    private Span createStatusBadge(DoctorResponse d) {
        String label = d.available() ? "موجود" : "غیرفعال";
        String color = d.available() ? "#059669" : "#DC2626";
        String bg = d.available() ? "#D1FAE5" : "#FEE2E2";
        Span badge = new Span(label);
        badge.getStyle()
                .set("font-size", "11px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "10px")
                .set("color", color)
                .set("background", bg);
        return badge;
    }

    private Component createActions(DoctorResponse d) {
        Button edit = new Button(VaadinIcon.EDIT.create());
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        edit.setTooltipText("ویرایش");
        edit.addClickListener(e -> openDoctorDialog(d));

        Button delete = new Button(VaadinIcon.TRASH.create());
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        delete.setTooltipText("حذف");
        delete.addClickListener(e -> confirmDelete(d));

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(false);
        actions.getStyle().set("gap", "4px");
        return actions;
    }

    // ============================================
    // بارگذاری + فیلتر
    // ============================================
    private void loadDoctors() {
        allDoctors = doctorService.getAllDoctors();

        List<String> specs = allDoctors.stream()
                .map(DoctorResponse::specialization)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();
        specFilter.setItems(specs);

        applyFilters();
    }

    private void applyFilters() {
        if (allDoctors == null) {
            grid.setItems(List.of());
            return;
        }

        String query = searchField.getValue();
        String spec = specFilter.getValue();
        boolean onlyAvail = availableOnly.getValue();

        List<DoctorResponse> filtered = allDoctors.stream()
                .filter(d -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.trim().toLowerCase();
                        String name = (d.firstName() + " " + d.lastName()).toLowerCase();
                        if (!name.contains(q)) return false;
                    }
                    if (spec != null && !spec.isBlank()) {
                        if (!spec.equals(d.specialization())) return false;
                    }
                    if (onlyAvail && !d.available()) return false;
                    return true;
                })
                .toList();

        grid.setItems(filtered);
    }

    // ============================================
    // Dialog فرم ایجاد/ویرایش
    // ============================================
    private void openDoctorDialog(DoctorResponse existing) {
        boolean isEdit = existing != null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEdit ? "ویرایش پزشک" : "افزودن پزشک جدید");
        dialog.setWidth("700px");
        dialog.setMaxWidth("95vw");

        // فیلدها
        TextField firstName = new TextField("نام");
        firstName.setRequiredIndicatorVisible(true);
        firstName.setWidthFull();

        TextField lastName = new TextField("نام خانوادگی");
        lastName.setRequiredIndicatorVisible(true);
        lastName.setWidthFull();

        EmailField email = new EmailField("ایمیل");
        email.setWidthFull();

        TextField phone = new TextField("تلفن");
        phone.setWidthFull();

        TextField specialization = new TextField("تخصص");
        specialization.setRequiredIndicatorVisible(true);
        specialization.setWidthFull();

        IntegerField yearsOfExperience = new IntegerField("سابقه (سال)");
        yearsOfExperience.setMin(0);
        yearsOfExperience.setMax(80);
        yearsOfExperience.setWidthFull();

        BigDecimalField consultationFee = new BigDecimalField("هزینه ویزیت (تومان)");
        consultationFee.setWidthFull();

        TextArea bio = new TextArea("بیوگرافی");
        bio.setMaxLength(1000);
        bio.setWidthFull();
        bio.setHeight("100px");

        Checkbox available = new Checkbox("پزشک موجود است");
        available.setValue(true);

        // فرم‌لیوت
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        form.add(firstName, lastName, email, phone, specialization, yearsOfExperience);
        form.setColspan(specialization, 2);
        form.add(consultationFee, available);
        form.setColspan(available, 2);
        form.add(bio);
        form.setColspan(bio, 2);

        // پر کردن فرم در حالت ویرایش
        if (isEdit) {
            firstName.setValue(existing.firstName() != null ? existing.firstName() : "");
            lastName.setValue(existing.lastName() != null ? existing.lastName() : "");
            email.setValue(existing.email() != null ? existing.email() : "");
            phone.setValue(existing.phone() != null ? existing.phone() : "");
            specialization.setValue(existing.specialization() != null ? existing.specialization() : "");
            yearsOfExperience.setValue(existing.yearsOfExperience());
            consultationFee.setValue(existing.consultationFee());
            bio.setValue(existing.bio() != null ? existing.bio() : "");
            available.setValue(existing.available());
        }

        // دکمه‌ها
        Button save = new Button(isEdit ? "ذخیره تغییرات" : "افزودن");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            // اعتبارسنجی
            if (firstName.getValue() == null || firstName.getValue().isBlank()) {
                Notification.show("نام الزامی است", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (lastName.getValue() == null || lastName.getValue().isBlank()) {
                Notification.show("نام خانوادگی الزامی است", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (specialization.getValue() == null || specialization.getValue().isBlank()) {
                Notification.show("تخصص الزامی است", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                if (isEdit) {
                    UpdateDoctorRequest req = new UpdateDoctorRequest(
                            firstName.getValue().trim(),
                            lastName.getValue().trim(),
                            nullIfBlank(email.getValue()),
                            nullIfBlank(phone.getValue()),
                            specialization.getValue().trim(),
                            nullIfBlank(bio.getValue()),
                            yearsOfExperience.getValue(),
                            consultationFee.getValue(),
                            available.getValue()
                    );
                    doctorService.updateDoctor(existing.id(), req);
                    Notification.show("پزشک با موفقیت ویرایش شد", 3000,
                            Notification.Position.TOP_CENTER);
                } else {
                    CreateDoctorRequest req = new CreateDoctorRequest(
                            firstName.getValue().trim(),
                            lastName.getValue().trim(),
                            nullIfBlank(email.getValue()),
                            nullIfBlank(phone.getValue()),
                            specialization.getValue().trim(),
                            nullIfBlank(bio.getValue()),
                            yearsOfExperience.getValue(),
                            consultationFee.getValue(),
                            available.getValue()
                    );
                    doctorService.createDoctor(req);
                    Notification.show("پزشک با موفقیت افزوده شد", 3000,
                            Notification.Position.TOP_CENTER);
                }
                dialog.close();
                loadDoctors();
            } catch (Exception ex) {
                Notification.show("خطا: " + ex.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        });

        Button cancel = new Button("انصراف", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    // ============================================
    // حذف
    // ============================================
    private void confirmDelete(DoctorResponse d) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("حذف پزشک");
        confirm.setText("آیا از حذف «دکتر " + d.firstName() + " " + d.lastName()
                + "» مطمئن هستید؟ این عمل قابل بازگشت نیست.");
        confirm.setCancelable(true);
        confirm.setCancelText("انصراف");
        confirm.setConfirmText("بله، حذف کن");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            try {
                doctorService.deleteDoctor(d.id());
                Notification.show("پزشک حذف شد", 3000, Notification.Position.TOP_CENTER);
                loadDoctors();
            } catch (Exception ex) {
                Notification.show("خطا در حذف: " + ex.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        });
        confirm.open();
    }

    // ============================================
    // کمکی‌ها
    // ============================================
    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
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