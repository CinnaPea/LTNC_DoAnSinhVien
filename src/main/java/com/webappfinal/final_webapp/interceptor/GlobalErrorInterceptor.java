package com.webappfinal.final_webapp.interceptor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.webappfinal.final_webapp.dto.DashboardUserDTO;
import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.DashboardService;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalErrorInterceptor {
    private final AuthSessionService authSessionService;
    private final DashboardService dashboardService;

    public GlobalErrorInterceptor(AuthSessionService authSessionService, DashboardService dashboardService) {
        this.authSessionService = authSessionService;
        this.dashboardService = dashboardService;
    }

    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception e, HttpServletRequest req, Model model) {
        model.addAttribute("errorType", e.getClass().getSimpleName());
        model.addAttribute("backUrl", "/dashboard");
        model.addAttribute("role", null);

        String us = authSessionService.getAuthenticatedUsername(req);
        if (us != null && !us.isBlank()) {
            try {
                DashboardUserDTO user = dashboardService.getDashboardUser(us);
                model.addAttribute("user", user);
                model.addAttribute("role", user.getLoaiTK());
                model.addAttribute("backUrl", "/dashboard");
            } catch (Exception ex) {
                model.addAttribute("backUrl", "/login");
            }
        } else {
            model.addAttribute("backUrl", "/login");
        }
        return "common/error";
    }
}
