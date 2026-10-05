package com.medapp.ui.view.admin;

import com.medapp.ui.dto.UserResponse;
import com.medapp.ui.layout.MainLayout;
import com.medapp.ui.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("مدیریت کاربران | MedApp")
@RolesAllowed("ADMIN")
public class ManageUsersView extends VerticalLayout {

    private final UserService userService;

    private final Grid<UserResponse> grid = new Grid<>(UserResponse.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<RoleFilter> roleFilter = new ComboBox<>();

    private List<UserResponse> allUsers;

    private static final DateTimeFormatter CREATED_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public ManageUsersView(UserService userService) {
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

        loadUsers();
    }

    // ============================================
    // هدر
    // ============================================
    private Component createHeader() {
        H2 title = new H2("مدیریت کاربران");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#1F2937");

        Paragraph subtitle = new Paragraph("مشاهده و مدیریت کاربران سیستم");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "14px");

        Button back = new Button("بازگشت", VaadinIcon.ARROW_RIGHT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("admin")));

        HorizontalLayout titleRow = new HorizontalLayout(
                new VerticalLayout(title, subtitle),
                back
        );
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.getChildren().findFirst().ifPresent(c -> {
            ((VerticalLayout) c).setPadding(false);
            ((VerticalLayout) c).setSpacing(false);
        });
        return titleRow;
    }

    // ============================================
    // نوار فیلتر
    // ============================================
    private Component createFilterBar() {
        searchField.setPlaceholder("جستجوی نام یا ایمیل...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> applyFilters());

        roleFilter.setLabel("نقش");
        roleFilter.setItems(RoleFilter.values());
        roleFilter.setValue(RoleFilter.ALL);
        roleFilter.setWidth("180px");
        roleFilter.addValueChangeListener(e -> applyFilters());

        HorizontalLayout bar = new HorizontalLayout(searchField, roleFilter);
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

        // ستون ID
        grid.addColumn(u -> toPersianDigits(String.valueOf(u.id())))
                .setHeader("شناسه")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // ستون نام
        grid.addColumn(u -> u.firstName() + " " + u.lastName())
                .setHeader("نام و نام خانوادگی")
                .setAutoWidth(true)
                .setFlexGrow(2);

        // ستون ایمیل
        grid.addColumn(UserResponse::email)
                .setHeader("ایمیل")
                .setAutoWidth(true)
                .setFlexGrow(2);

        // ستون تلفن
        grid.addColumn(u -> u.phone() != null ? toPersianDigits(u.phone()) : "—")
                .setHeader("تلفن")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // ستون نقش
        grid.addComponentColumn(this::createRoleBadge)
                .setHeader("نقش")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // ستون وضعیت
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("وضعیت")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // ستون تاریخ عضویت
        grid.addColumn(u -> u.createdAt() != null
                        ? toPersianDigits(u.createdAt().format(CREATED_FMT))
                        : "—")
                .setHeader("تاریخ عضویت")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // ستون اکشن
        grid.addComponentColumn(this::createActions)
                .setHeader("عملیات")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private Component createGridCard() {
        H3 title = new H3("لیست کاربران");
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
    // badge نقش
    // ============================================
    private Span createRoleBadge(UserResponse user) {
        String label;
        String color;
        String bg;
        if ("ADMIN".equals(user.role())) {
            label = "مدیر";
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
                .set("padding", "3px 10px")
                .set("border-radius", "10px")
                .set("color", color)
                .set("background", bg);
        return badge;
    }

    // ============================================
    // badge وضعیت
    // ============================================
    private Span createStatusBadge(UserResponse user) {
        String label = user.enabled() ? "فعال" : "غیرفعال";
        String color = user.enabled() ? "#059669" : "#DC2626";
        String bg = user.enabled() ? "#D1FAE5" : "#FEE2E2";

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

    // ============================================
    // دکمه‌های عملیات
    // ============================================
    private Component createActions(UserResponse user) {
        Button delete = new Button(VaadinIcon.TRASH.create());
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        delete.setTooltipText("حذف کاربر");
        delete.addClickListener(e -> confirmDelete(user));

        HorizontalLayout actions = new HorizontalLayout(delete);
        actions.setSpacing(false);
        actions.getStyle().set("gap", "4px");
        return actions;
    }

    // ============================================
    // بارگذاری کاربران
    // ============================================
    private void loadUsers() {
        allUsers = userService.getAllUsers();
        applyFilters();
    }

    // ============================================
    // اعمال فیلترها
    // ============================================
    private void applyFilters() {
        if (allUsers == null) {
            grid.setItems(List.of());
            return;
        }

        String query = searchField.getValue();
        RoleFilter role = roleFilter.getValue();

        List<UserResponse> filtered = allUsers.stream()
                .filter(u -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.trim().toLowerCase();
                        String name = (u.firstName() + " " + u.lastName()).toLowerCase();
                        String email = u.email() != null ? u.email().toLowerCase() : "";
                        if (!name.contains(q) && !email.contains(q)) return false;
                    }
                    if (role != null && role != RoleFilter.ALL) {
                        if (!role.getRoleValue().equals(u.role())) return false;
                    }
                    return true;
                })
                .toList();

        grid.setItems(filtered);
    }

    // ============================================
    // تأیید حذف
    // ============================================
    private void confirmDelete(UserResponse user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("حذف کاربر");
        dialog.setText("آیا از حذف «" + user.firstName() + " " + user.lastName()
                + "» مطمئن هستید؟ این عمل قابل بازگشت نیست.");
        dialog.setCancelable(true);
        dialog.setCancelText("انصراف");
        dialog.setConfirmText("بله، حذف کن");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                userService.deleteUser(user.id());
                Notification.show("کاربر با موفقیت حذف شد", 3000,
                        Notification.Position.TOP_CENTER);
                loadUsers();
            } catch (Exception ex) {
                Notification.show("خطا در حذف کاربر: " + ex.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        });
        dialog.open();
    }

    // ============================================
    // enum فیلتر نقش
    // ============================================
    private enum RoleFilter {
        ALL("همه", null),
        CUSTOMER("کاربر", "CUSTOMER"),
        ADMIN("مدیر", "ADMIN");

        private final String label;
        private final String roleValue;

        RoleFilter(String label, String roleValue) {
            this.label = label;
            this.roleValue = roleValue;
        }

        public String getRoleValue() {
            return roleValue;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // ============================================
    // کمکی
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
}