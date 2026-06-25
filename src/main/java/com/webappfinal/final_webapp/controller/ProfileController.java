package com.webappfinal.final_webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webappfinal.final_webapp.dto.UserProfileForm;
import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ProfileController {
    private final AuthSessionService authSessionService;
    private final ProfileService profileService;

    public ProfileController(AuthSessionService authSessionService, ProfileService profileService) {
        this.authSessionService = authSessionService;
        this.profileService = profileService;
    }

    @GetMapping("/student/profile")
    public String studentProfile(HttpServletRequest request, Model model) {
        String username = requireUsername(request);
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", profileService.getStudentProfile(username));
        }
        return "student/profile";
    }

    @PostMapping("/student/profile")
    public String updateStudentProfile(
            @ModelAttribute("profileForm") UserProfileForm profileForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String username = requireUsername(request);
        try {
            profileService.updateStudentProfile(username, profileForm);
            redirectAttributes.addFlashAttribute("success", "Thông tin sinh viên đã được cập nhật.");
            return "redirect:/student/profile";
        } catch (IllegalArgumentException ex) {
            profileForm.setUsername(profileService.getStudentProfile(username).getUsername());
            profileForm.setProfileCode(profileService.getStudentProfile(username).getProfileCode());
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("profileForm", profileForm);
            return "redirect:/student/profile";
        }
    }

    @PostMapping("/student/profile/delete")
    public String deleteStudentAccount(
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        String username = requireUsername(request);
        try {
            profileService.deleteAccount(username);
            authSessionService.logout(response);
            redirectAttributes.addFlashAttribute("success", "Tai khoan da duoc xoa.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/student/profile";
        }
    }

    @GetMapping("/instructor/profile")
    public String instructorProfile(HttpServletRequest request, Model model) {
        String username = requireUsername(request);
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", profileService.getInstructorProfile(username));
        }
        return "instructor/profile";
    }

    @PostMapping("/instructor/profile")
    public String updateInstructorProfile(
            @ModelAttribute("profileForm") UserProfileForm profileForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String username = requireUsername(request);
        try {
            profileService.updateInstructorProfile(username, profileForm);
            redirectAttributes.addFlashAttribute("success", "Thông tin giảng viên đã được cập nhật.");
            return "redirect:/instructor/profile";
        } catch (IllegalArgumentException ex) {
            profileForm.setUsername(profileService.getInstructorProfile(username).getUsername());
            profileForm.setProfileCode(profileService.getInstructorProfile(username).getProfileCode());
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("profileForm", profileForm);
            return "redirect:/instructor/profile";
        }
    }

    @PostMapping("/instructor/profile/delete")
    public String deleteInstructorAccount(
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        String username = requireUsername(request);
        try {
            profileService.deleteAccount(username);
            authSessionService.logout(response);
            redirectAttributes.addFlashAttribute("success", "Tai khoan da duoc xoa.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/instructor/profile";
        }
    }

    private String requireUsername(HttpServletRequest request) {
        String username = authSessionService.getAuthenticatedUsername(request);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("You need to log in first.");
        }
        return username;
    }
}
