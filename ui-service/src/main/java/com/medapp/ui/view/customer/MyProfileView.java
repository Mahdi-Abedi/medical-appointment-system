package com.medapp.ui.view.customer;

import com.medapp.ui.dto.UpdateUserRequest;
import com.medapp.ui.dto.UserResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.SessionService;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("پروفایل من | MedApp")
@PermitAll
public class MyProfileView extends VerticalLayout {

    private final SessionService sessionService;
    private final UserService userService;

    private UserResponse currentUser;

    // فرم
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField phoneField = new TextField();
    private final EmailField emailField = new EmailField();
    private Button saveButton;

    private static final DateTimeFormatter CREATED_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public MyProfileView(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.currentUser = sessionService.getCurrentUser();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#F9FAFB")
                .set("padding", "24px");

        configureForm();

        add(
                createHeader(),
                createProfileCard(),
                createFormCard(),
                createAccountInfoCard(),
                createActionButtons()
        );

        populateForm();
    }

    // ============================================
    // هدر
    // ============================================
    private Component createHeader() {
        H2 title = new H2("پروفایل من");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("مشاهده و ویرایش اطلاعات شخصی");
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
    // کارت پروفایل (آواتار + نام + ایمیل)
    // ============================================
    private Component createProfileCard() {
        if (currentUser == null) {
            return new Span();
        }

        String initials = getInitials(currentUser.firstName(), currentUser.lastName());
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

        H3 name = new H3(currentUser.firstName() + " " + currentUser.lastName());
        name.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Span email = new Span(currentUser.email());
        email.getStyle()
                .set("font-size", "14px")
                .set("color", "#6B7280");

        Span roleBadge = createRoleBadge(currentUser.role());

        VerticalLayout textCol = new VerticalLayout(name, email, roleBadge);
        textCol.setPadding(false);
        textCol.setSpacing(false);
        textCol.getStyle().set("gap", "6px");
        textCol.setAlignItems(FlexComponent.Alignment.START);

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
    // کانفیگ فرم
    // ============================================
    private void configureForm() {
        firstNameField.setLabel("نام");
        firstNameField.setRequiredIndicatorVisible(true);
        firstNameField.setWidthFull();

        lastNameField.setLabel("نام خانوادگی");
        lastNameField.setRequiredIndicatorVisible(true);
        lastNameField.setWidthFull();

        phoneField.setLabel("شماره تماس");
        phoneField.setPlaceholder("۰۹۱۲...");
        phoneField.setWidthFull();

        emailField.setLabel("ایمیل");
        emailField.setReadOnly(true);
        emailField.setHelperText("ایمیل قابل ویرایش نیست");
        emailField.setWidthFull();
    }

    // ============================================
    // کارت فرم
    // ============================================
    private Component createFormCard() {
        H3 title = new H3("اطلاعات شخصی");
        title.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "16px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        VerticalLayout form = new VerticalLayout(
                firstNameField,
                lastNameField,
                phoneField,
                emailField
        );
        form.setPadding(false);
        form.setSpacing(true);
        form.getStyle().set("gap", "16px");

        VerticalLayout card = new VerticalLayout(title, form);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)")
                .set("max-width", "600px");
        return card;
    }

    // ============================================
    // کارت اطلاعات اکانت (تاریخ عضویت، وضعیت)
    // ============================================
    private Component createAccountInfoCard() {
        if (currentUser == null) {
            return new Span();
        }

        H3 title = new H3("اطلاعات حساب");
        title.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "16px")
                .set("font-weight", "600")
                .set("color", "#1F2937");

        String createdStr = currentUser.createdAt() != null
                ? toPersianDigits(currentUser.createdAt().format(CREATED_FMT))
                : "—";

        VerticalLayout content = new VerticalLayout(
                createInfoRow(VaadinIcon.USER, "شناسه کاربر",
                        toPersianDigits(String.valueOf(currentUser.id())), "#7C3AED"),
                createInfoRow(VaadinIcon.CALENDAR, "تاریخ عضویت", createdStr, "#F59E0B"),
                createInfoRow(VaadinIcon.CHECK_CIRCLE,
                        "وضعیت حساب",
                        currentUser.enabled() ? "فعال" : "غیرفعال",
                        currentUser.enabled() ? "#059669" : "#DC2626")
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
                .set("max-width", "600px");
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
    // دکمه‌های اکشن
    // ============================================
    private Component createActionButtons() {
        saveButton = new Button("ذخیره تغییرات", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        saveButton.addClickListener(e -> save());

        Button reset = new Button("بازنشانی");
        reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        reset.addClickListener(e -> populateForm());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, reset);
        buttons.setSpacing(true);
        buttons.getStyle().set("gap", "12px").set("flex-wrap", "wrap");
        return buttons;
    }

    // ============================================
    // پر کردن فرم از currentUser
    // ============================================
    private void populateForm() {
        currentUser = sessionService.getCurrentUser();
        if (currentUser == null) {
            Notification.show("اطلاعات کاربر یافت نشد. لطفاً دوباره وارد شوید.",
                    3000, Notification.Position.MIDDLE);
            return;
        }
        firstNameField.setValue(currentUser.firstName() != null ? currentUser.firstName() : "");
        lastNameField.setValue(currentUser.lastName() != null ? currentUser.lastName() : "");
        phoneField.setValue(currentUser.phone() != null ? currentUser.phone() : "");
        emailField.setValue(currentUser.email() != null ? currentUser.email() : "");
    }

    // ============================================
    // ذخیره تغییرات
    // ============================================
    private void save() {
        if (currentUser == null || currentUser.id() == null) {
            Notification.show("اطلاعات کاربر یافت نشد. لطفاً دوباره وارد شوید.",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        String firstName = firstNameField.getValue();
        String lastName = lastNameField.getValue();

        if (firstName == null || firstName.isBlank()) {
            Notification.show("نام نمی‌تواند خالی باشد", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (lastName == null || lastName.isBlank()) {
            Notification.show("نام خانوادگی نمی‌تواند خالی باشد", 3000, Notification.Position.MIDDLE);
            return;
        }

        saveButton.setEnabled(false);

        try {
            UpdateUserRequest request = new UpdateUserRequest(
                    firstName.trim(),
                    lastName.trim(),
                    phoneField.getValue() != null ? phoneField.getValue().trim() : null
            );

            UserResponse updated = userService.updateUser(currentUser.id(), request);

            // به‌روزرسانی SessionService
            sessionService.setCurrentUser(updated);
            this.currentUser = updated;

            Notification.show("پروفایل با موفقیت به‌روزرسانی شد", 3000,
                    Notification.Position.TOP_CENTER);

            // رفرش UI
            removeAll();
            add(
                    createHeader(),
                    createProfileCard(),
                    createFormCard(),
                    createAccountInfoCard(),
                    createActionButtons()
            );
            populateForm();

        } catch (Exception ex) {
            Notification.show("خطا در ذخیره تغییرات: " + ex.getMessage(), 5000,
                    Notification.Position.MIDDLE);
        } finally {
            saveButton.setEnabled(true);
        }
    }

    // ============================================
    // badge نقش
    // ============================================
    private Span createRoleBadge(String role) {
        String label;
        String color;
        String bg;

        if ("ADMIN".equals(role)) {
            label = "مدیر سیستم";
            color = "#DC2626";
            bg = "#FEE2E2";
        } else {
            label = "کاربر";
            color = "#7C3AED";
            bg = "#EDE9FE";
        }

        Span badge = new Span(label);
        badge.getStyle()
                .set("font-size", "11px")
                .set("font-weight", "600")
                .set("padding", "4px 12px")
                .set("border-radius", "10px")
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
}