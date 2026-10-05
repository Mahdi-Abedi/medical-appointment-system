package com.medapp.ui.layout;

import com.medapp.ui.dto.UserResponse;
import com.medapp.ui.service.AuthService;
import com.medapp.ui.service.SessionService;
import com.medapp.ui.view.auth.LoginView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import jakarta.annotation.security.PermitAll;
import org.springframework.stereotype.Component;

@PermitAll
public class MainLayout extends AppLayout {

    private final SessionService sessionService;
    private final AuthService authService;

    public MainLayout(SessionService sessionService, AuthService authService) {
        this.sessionService = sessionService;
        this.authService = authService;

        setPrimarySection(Section.DRAWER);
        addClassName("main-layout");
        createHeader();
        createDrawer();
    }

    // ========================================
    // Header (بالای صفحه)
    // ========================================
    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        H1 logo = new H1("MedApp");
        logo.getStyle()
                .set("font-size", "22px")
                .set("margin", "0")
                .set("color", "#7C3AED")
                .set("font-weight", "700");

        HorizontalLayout titleLayout = new HorizontalLayout(toggle, logo);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(titleLayout, createUserSection());
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("padding", "4px 12px");
        header.expand(titleLayout);

        addToNavbar(header);
    }

    private HorizontalLayout createUserSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.setAlignItems(FlexComponent.Alignment.CENTER);
        section.setSpacing(true);

        if (sessionService.isLoggedIn() && sessionService.getCurrentUser() != null) {
            UserResponse user = sessionService.getCurrentUser();

            Avatar avatar = new Avatar(user.firstName() + " " + user.lastName());
            avatar.getStyle().set("background-color", "#7C3AED");
            avatar.getStyle().set("color", "white");
            avatar.getStyle().set("font-weight", "600");

            Span nameSpan = new Span(user.firstName() + " " + user.lastName());
            nameSpan.getStyle().set("font-weight", "500");
            nameSpan.getStyle().set("font-size", "14px");

            Button logout = new Button("خروج");
            logout.setIcon(VaadinIcon.SIGN_OUT.create());
            logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            logout.addClickListener(e -> {
                authService.logout();
                UI.getCurrent().navigate(LoginView.class);
            });

            HorizontalLayout userBlock = new HorizontalLayout(avatar, nameSpan);
            userBlock.setAlignItems(FlexComponent.Alignment.CENTER);
            userBlock.getStyle().set("cursor", "pointer").set("gap", "8px");
            userBlock.addClickListener(e ->
                    UI.getCurrent().navigate("profile"));

            section.add(userBlock, logout);
        }

        return section;
    }

    // ========================================
    // Drawer (سایدبار)
    // ========================================
    private void createDrawer() {
        SideNav nav = new SideNav();
        nav.getStyle().set("padding", "16px 8px");

        nav.addItem(new SideNavItem("داشبورد", "/", VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("پزشکان", "/doctors", VaadinIcon.DOCTOR.create()));
        nav.addItem(new SideNavItem("نوبت‌های من", "appointments", VaadinIcon.CALENDAR.create()));

        // اگر کاربر ادمین است، منوی ادمین را هم اضافه کن
        if (sessionService.isAdmin()) {
            SideNav adminNav = new SideNav();
            adminNav.setLabel("مدیریت سیستم");
            adminNav.getStyle()
                    .set("padding", "16px 8px")
                    .set("border-top", "1px solid #E5E7EB")
                    .set("margin-top", "16px");

            adminNav.addItem(new SideNavItem("داشبورد ادمین", "/admin", VaadinIcon.DASHBOARD.create()));
            adminNav.addItem(new SideNavItem("کاربران", "/admin/users", VaadinIcon.USERS.create()));
            adminNav.addItem(new SideNavItem("مدیریت پزشکان", "/admin/doctors", VaadinIcon.DOCTOR.create()));
            adminNav.addItem(new SideNavItem("همه‌ی نوبت‌ها", "/admin/appointments", VaadinIcon.CALENDAR.create()));

            addToDrawer(nav, adminNav);
        } else {
            addToDrawer(nav);
        }
    }
}