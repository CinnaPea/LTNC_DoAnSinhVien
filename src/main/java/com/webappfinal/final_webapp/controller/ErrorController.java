package com.webappfinal.final_webapp.controller;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    @GetMapping("/errortest")
    public String handleError(Model model) {
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", 404);
        model.addAttribute("errorType", "Page Not Found");
        model.addAttribute("message", "The page you are looking for does not exist.");
        return "common/error";
    }
}
