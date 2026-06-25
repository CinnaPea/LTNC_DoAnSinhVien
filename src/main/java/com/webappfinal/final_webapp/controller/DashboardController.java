package com.webappfinal.final_webapp.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.webappfinal.final_webapp.dto.DashboardUserDTO;
import com.webappfinal.final_webapp.dto.DangKyCatalogView;
import com.webappfinal.final_webapp.dto.DoAnCatalogView;
import com.webappfinal.final_webapp.dto.TheLoaiCatalogView;
import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.DangKyApiService;
import com.webappfinal.final_webapp.service.DashboardService;
import com.webappfinal.final_webapp.service.DoAnApiService;
import com.webappfinal.final_webapp.service.TheLoaiApiService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DashboardController {
    private final DashboardService dashboardService;
    private final AuthSessionService authSessionService;
    private final TheLoaiApiService theLoaiApiService;
    private final DangKyApiService dangKyApiService;
    private final DoAnApiService doAnApiService;

    public DashboardController(
            DashboardService dashboardService,
            AuthSessionService authSessionService,
            TheLoaiApiService theLoaiApiService,
            DangKyApiService dangKyApiService,
            DoAnApiService doAnApiService) {
        this.dashboardService = dashboardService;
        this.authSessionService = authSessionService;
        this.theLoaiApiService = theLoaiApiService;
        this.dangKyApiService = dangKyApiService;
        this.doAnApiService = doAnApiService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest req, Model model) {
        String us = authSessionService.getAuthenticatedUsername(req);
        if (us == null || us.isBlank()) {
            return "redirect:/login";
        }
        DashboardUserDTO user = dashboardService.getDashboardUser(us);
        model.addAttribute("user", user);
        return switch (user.getLoaiTK()) {
            case "SINH_VIEN" -> {
                populateStudentDashboard(model, user.getProfileCode());
                yield "student/dashboard";
            }
            case "GIANG_VIEN" -> {
                populateInstructorDashboard(model, user.getProfileCode());
                yield "instructor/dashboard";
            }
            case "ADMIN" -> "redirect:/admin/dashboard";
            default -> "redirect:/index";
        };
    }

    private void populateStudentDashboard(Model model, String svId) {
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForStudent(svId);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForStudent(svId);

        long pendingRegistrationCount = registrationCatalog.registrations().stream()
            .filter(registration -> registration.isPending())
            .count();
        long approvedRegistrationCount = registrationCatalog.registrations().stream()
            .filter(registration -> registration.isApproved())
            .count();
        long rejectedRegistrationCount = registrationCatalog.registrations().stream()
            .filter(registration -> registration.isRejected())
            .count();
        long otherRegistrationCount = registrationCatalog.registrations().size()
            - pendingRegistrationCount
            - approvedRegistrationCount
            - rejectedRegistrationCount;
        long activeThesisCount = thesisCatalog.theses().stream().filter(thesis -> !thesis.isCompleted()).count();
        long completedThesisCount = thesisCatalog.theses().stream().filter(thesis -> thesis.isCompleted()).count();

        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("studentRegistrationCount", registrationCatalog.registrations().size());
        model.addAttribute("pendingRegistrationCount", pendingRegistrationCount);
        model.addAttribute("approvedRegistrationCount", approvedRegistrationCount);
        model.addAttribute("rejectedRegistrationCount", rejectedRegistrationCount);
        model.addAttribute("otherRegistrationCount", otherRegistrationCount);
        model.addAttribute("activeThesisCount", activeThesisCount);
        model.addAttribute("completedThesisCount", completedThesisCount);
    }

    private void populateInstructorDashboard(Model model, String gvId) {
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopicsForLecturer(gvId);
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForLecturer(gvId);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForLecturer(gvId);

        int topicTotal = topicCatalog.topics().size();
        long openTopicCount = topicCatalog.topics().stream().filter(topic -> topic.isOpen()).count();
        long closedTopicCount = topicTotal - openTopicCount;
        int topicOpenWidth = percentage(openTopicCount, topicTotal);

        Set<String> thesisRegistrationIds = thesisCatalog.theses().stream()
            .map(thesis -> thesis.getDkId())
            .filter(dkId -> dkId != null && !dkId.isBlank())
            .collect(Collectors.toSet());
        long supervisedStudentCount = registrationCatalog.registrations().stream()
            .filter(registration -> thesisRegistrationIds.contains(registration.getDkId()))
            .map(registration -> registration.getSvId())
            .filter(svId -> svId != null && !svId.isBlank())
            .distinct()
            .count();
        long completedThesisCount = thesisCatalog.theses().stream().filter(thesis -> thesis.isCompleted()).count();
        long activeThesisCount = thesisCatalog.theses().size() - completedThesisCount;

        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("topicTotal", topicTotal);
        model.addAttribute("openTopicCount", openTopicCount);
        model.addAttribute("closedTopicCount", closedTopicCount);
        model.addAttribute("topicOpenWidth", topicOpenWidth);
        model.addAttribute("supervisedStudentCount", supervisedStudentCount);
        model.addAttribute("activeThesisCount", activeThesisCount);
        model.addAttribute("completedThesisCount", completedThesisCount);
    }

    private int percentage(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round(value * 100.0 / total);
    }
}
