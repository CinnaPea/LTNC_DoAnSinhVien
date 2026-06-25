package com.webappfinal.final_webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webappfinal.final_webapp.dto.SignupForm;
import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.SignupService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController {
    private final AuthSessionService authSessionService;
    private final SignupService signupService;

    public AuthController(AuthSessionService authSessionService, SignupService signupService) {
        this.authSessionService = authSessionService;
        this.signupService = signupService;
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest req) {
        if (authSessionService.isAuthenticated(req)) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupPage(HttpServletRequest request, Model model) {
        if (authSessionService.isAuthenticated(request)) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "auth/signup";
    }

    @PostMapping("/login") 
    public String login(@RequestParam("username") String us, @RequestParam("password") String pass,
                        HttpServletResponse resp, Model model) {
        if (authSessionService.isValidCredentials(us, pass)) {
            authSessionService.login(resp, us);
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Wrong username or password!");
        model.addAttribute("enteredUsername", us);
        return "auth/login";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("signupForm") SignupForm signupForm, RedirectAttributes redirectAttributes) {
        try {
            signupService.register(signupForm);
            redirectAttributes.addFlashAttribute("success", "Account created successfully. You can now log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("signupForm", signupForm);
            return "redirect:/signup";
        }
    }

    @GetMapping("/logout") 
    public String logout(HttpServletResponse resp) {
        authSessionService.logout(resp);
        return "redirect:/login";
    }
}
