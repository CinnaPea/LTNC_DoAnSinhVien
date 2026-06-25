package com.webappfinal.final_webapp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.webappfinal.final_webapp.service.AuthSessionService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {
    private final AuthSessionService authSessionService;

    public HomeController(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        if (authSessionService.isAuthenticated(request)) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
}
