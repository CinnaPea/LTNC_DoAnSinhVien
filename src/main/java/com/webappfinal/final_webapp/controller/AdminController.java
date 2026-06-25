package com.webappfinal.final_webapp.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webappfinal.final_webapp.dto.AdminRegistrationView;
import com.webappfinal.final_webapp.dto.AdminThesisView;
import com.webappfinal.final_webapp.dto.AdminTopicView;
import com.webappfinal.final_webapp.dto.AdminUserApiItem;
import com.webappfinal.final_webapp.dto.AdminUserCatalogView;
import com.webappfinal.final_webapp.dto.AdminUserForm;
import com.webappfinal.final_webapp.dto.DangKyApiItem;
import com.webappfinal.final_webapp.dto.DangKyCatalogView;
import com.webappfinal.final_webapp.dto.DoAnApiItem;
import com.webappfinal.final_webapp.dto.DoAnCatalogView;
import com.webappfinal.final_webapp.dto.TheLoaiApiItem;
import com.webappfinal.final_webapp.dto.TheLoaiCatalogView;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;
import com.webappfinal.final_webapp.service.AdminUserApiException;
import com.webappfinal.final_webapp.service.AdminUserApiService;
import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.DangKyApiService;
import com.webappfinal.final_webapp.service.DoAnApiService;
import com.webappfinal.final_webapp.service.TheLoaiApiService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final int USERS_PAGE_SIZE = 5;

    private final AdminUserApiService adminUserApiService;
    private final TheLoaiApiService theLoaiApiService;
    private final DangKyApiService dangKyApiService;
    private final DoAnApiService doAnApiService;
    private final AuthSessionService authSessionService;
    private final NguoiDungRepository nguoiDungRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;
    private final String demoUsername;

    public AdminController(
            AdminUserApiService adminUserApiService,
            TheLoaiApiService theLoaiApiService,
            DangKyApiService dangKyApiService,
            DoAnApiService doAnApiService,
            AuthSessionService authSessionService,
            NguoiDungRepository nguoiDungRepository,
            GiangVienRepository giangVienRepository,
            SinhVienRepository sinhVienRepository,
            @Value("${app.auth.demo.username:admin1}") String demoUsername) {
        this.adminUserApiService = adminUserApiService;
        this.theLoaiApiService = theLoaiApiService;
        this.dangKyApiService = dangKyApiService;
        this.doAnApiService = doAnApiService;
        this.authSessionService = authSessionService;
        this.nguoiDungRepository = nguoiDungRepository;
        this.giangVienRepository = giangVienRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.demoUsername = demoUsername;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(name = "userPage", defaultValue = "1") int userPage,
            HttpServletRequest request,
            Model model) {
        String adminUsername = requireAdmin(request);

        AdminUserCatalogView userCatalog = adminUserApiService.fetchUsers();
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrations();
        DoAnCatalogView thesisCatalog = doAnApiService.fetchTheses();

        List<AdminUserApiItem> users = sortedAcademicUsers(userCatalog);
        List<AdminTopicView> topicViews = buildTopicViews(topicCatalog.topics());
        List<AdminRegistrationView> registrationViews = buildRegistrationViews(
            registrationCatalog.registrations(),
            topicCatalog.topics());
        List<AdminThesisView> thesisViews = buildThesisViews(
            thesisCatalog.theses(),
            registrationCatalog.registrations(),
            topicCatalog.topics());

        addAdminShell(model, adminUsername, "dashboard");
        addUserModel(model, userCatalog, users, userPage);
        addTopicModel(model, topicCatalog, topicViews);
        addRegistrationModel(model, registrationCatalog, registrationViews);
        addThesisModel(model, thesisCatalog, thesisViews);
        addOverviewMetrics(model, users, topicViews, registrationViews, thesisViews);
        return "admin/dashboard";
    }

    @GetMapping("/topics")
    public String topics(HttpServletRequest request, Model model) {
        String adminUsername = requireAdmin(request);
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();
        List<AdminTopicView> topicViews = buildTopicViews(topicCatalog.topics());

        addAdminShell(model, adminUsername, "topics");
        addTopicModel(model, topicCatalog, topicViews);
        return "admin/topics";
    }

    @GetMapping("/registrations")
    public String registrations(HttpServletRequest request, Model model) {
        String adminUsername = requireAdmin(request);
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrations();
        List<AdminRegistrationView> registrationViews = buildRegistrationViews(
            registrationCatalog.registrations(),
            topicCatalog.topics());

        addAdminShell(model, adminUsername, "registrations");
        model.addAttribute("topicCatalog", topicCatalog);
        addRegistrationModel(model, registrationCatalog, registrationViews);
        return "admin/registrations";
    }

    @GetMapping("/thesis")
    public String thesis(HttpServletRequest request, Model model) {
        String adminUsername = requireAdmin(request);
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrations();
        DoAnCatalogView thesisCatalog = doAnApiService.fetchTheses();
        List<AdminThesisView> thesisViews = buildThesisViews(
            thesisCatalog.theses(),
            registrationCatalog.registrations(),
            topicCatalog.topics());

        addAdminShell(model, adminUsername, "thesis");
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        addThesisModel(model, thesisCatalog, thesisViews);
        return "admin/thesis";
    }

    @GetMapping("/users/new")
    public String newUserForm(HttpServletRequest request, Model model) {
        requireAdmin(request);
        AdminUserForm form = new AdminUserForm();
        form.setActive(true);
        populateFormModel(model, form, true, "Tao tai khoan moi");
        return "admin/user-form";
    }

    @GetMapping("/users/{ndId}/edit")
    public String editUserForm(@PathVariable String ndId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        requireAdmin(request);

        try {
            AdminUserApiItem user = adminUserApiService.fetchUser(ndId);
            if (!isAcademicUser(user)) {
                throw new AdminUserApiException("Chi co the quan ly tai khoan SV hoac GV trong man hinh nay.");
            }

            AdminUserForm form = new AdminUserForm();
            form.setNdId(user.getNdId());
            form.setUsername(user.getUsername());
            form.setEmail(user.getEmail());
            form.setVaiTroId(user.getVaiTroId());
            form.setProfileName(user.getProfileName());
            form.setActive(user.isActive());

            populateFormModel(model, form, false, "Cap nhat tai khoan");
            return "admin/user-form";
        } catch (AdminUserApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/users")
    public String users(HttpServletRequest request) {
        requireAdmin(request);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users")
    public String createUser(
            @ModelAttribute("form") AdminUserForm form,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        requireAdmin(request);

        try {
            normalizeAndValidateForm(form, true);
            AdminUserApiItem created = adminUserApiService.createUser(form);
            redirectAttributes.addFlashAttribute("success",
                "Da tao tai khoan " + created.getUsername() + " voi vai tro " + created.getVaiTroId() + ".");
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException | AdminUserApiException ex) {
            model.addAttribute("error", ex.getMessage());
            populateFormModel(model, form, true, "Tao tai khoan moi");
            return "admin/user-form";
        }
    }

    @PostMapping("/users/{ndId}")
    public String updateUser(
            @PathVariable String ndId,
            @ModelAttribute("form") AdminUserForm form,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        requireAdmin(request);

        try {
            form.setNdId(ndId);
            normalizeAndValidateForm(form, false);
            AdminUserApiItem updated = adminUserApiService.updateUser(ndId, form);
            redirectAttributes.addFlashAttribute("success",
                "Da cap nhat tai khoan " + updated.getUsername() + ".");
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException | AdminUserApiException ex) {
            model.addAttribute("error", ex.getMessage());
            populateFormModel(model, form, false, "Cap nhat tai khoan");
            return "admin/user-form";
        }
    }

    @PostMapping("/users/{ndId}/status/{active}")
    public String updateStatus(
            @PathVariable String ndId,
            @PathVariable boolean active,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        requireAdmin(request);

        try {
            adminUserApiService.updateStatus(ndId, active);
            redirectAttributes.addFlashAttribute("success",
                active ? "Nguoi dung da duoc kich hoat." : "Nguoi dung da duoc tam khoa.");
        } catch (AdminUserApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{ndId}/delete")
    public String deleteUser(
            @PathVariable String ndId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        requireAdmin(request);

        try {
            AdminUserApiItem user = adminUserApiService.fetchUser(ndId);
            if (!isAcademicUser(user)) {
                throw new AdminUserApiException("Chi co the xoa tai khoan SV hoac GV trong man hinh nay.");
            }

            adminUserApiService.deleteUser(ndId);
            redirectAttributes.addFlashAttribute("success", "Da xoa tai khoan " + user.getUsername() + ".");
        } catch (AdminUserApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    private void addAdminShell(Model model, String adminUsername, String activeSection) {
        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("activeSection", activeSection);
    }

    private void addUserModel(Model model, AdminUserCatalogView userCatalog, List<AdminUserApiItem> users, int requestedPage) {
        List<AdminUserApiItem> activeUsers = users.stream().filter(AdminUserApiItem::isActive).toList();
        List<AdminUserApiItem> inactiveUsers = users.stream().filter(user -> !user.isActive()).toList();
        List<AdminUserApiItem> studentUsers = users.stream().filter(user -> "SV".equalsIgnoreCase(user.getVaiTroId())).toList();
        List<AdminUserApiItem> lecturerUsers = users.stream().filter(user -> "GV".equalsIgnoreCase(user.getVaiTroId())).toList();
        int userTotalPages = calculateTotalPages(users.size(), USERS_PAGE_SIZE);
        int userCurrentPage = clampPage(requestedPage, userTotalPages);

        model.addAttribute("catalog", userCatalog);
        model.addAttribute("apiAvailable", userCatalog.apiAvailable());
        model.addAttribute("apiMessage", userCatalog.message());
        model.addAttribute("users", users);
        model.addAttribute("pagedUsers", pageItems(users, userCurrentPage, USERS_PAGE_SIZE));
        model.addAttribute("userCurrentPage", userCurrentPage);
        model.addAttribute("userTotalPages", userTotalPages);
        model.addAttribute("userPageNumbers", pageNumbers(userTotalPages));
        model.addAttribute("userPageStartIndex", (userCurrentPage - 1) * USERS_PAGE_SIZE);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("studentUsers", studentUsers);
        model.addAttribute("lecturerUsers", lecturerUsers);
    }

    private void addTopicModel(Model model, TheLoaiCatalogView topicCatalog, List<AdminTopicView> topicViews) {
        List<AdminTopicView> openTopics = topicViews.stream().filter(AdminTopicView::isOpen).toList();
        List<AdminTopicView> closedTopics = topicViews.stream().filter(view -> !view.isOpen()).toList();

        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("topicViews", topicViews);
        model.addAttribute("openTopicViews", openTopics);
        model.addAttribute("closedTopicViews", closedTopics);
    }

    private void addRegistrationModel(Model model, DangKyCatalogView registrationCatalog, List<AdminRegistrationView> registrationViews) {
        List<AdminRegistrationView> pendingRegistrations = registrationViews.stream().filter(AdminRegistrationView::isPending).toList();
        List<AdminRegistrationView> approvedRegistrations = registrationViews.stream().filter(AdminRegistrationView::isApproved).toList();
        List<AdminRegistrationView> rejectedRegistrations = registrationViews.stream().filter(view -> !view.isPending() && !view.isApproved()).toList();

        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("registrationViews", registrationViews);
        model.addAttribute("pendingRegistrations", pendingRegistrations);
        model.addAttribute("approvedRegistrations", approvedRegistrations);
        model.addAttribute("rejectedRegistrations", rejectedRegistrations);
    }

    private void addThesisModel(Model model, DoAnCatalogView thesisCatalog, List<AdminThesisView> thesisViews) {
        List<AdminThesisView> activeThesisViews = thesisViews.stream().filter(view -> !view.isCompleted()).toList();
        List<AdminThesisView> completedThesisViews = thesisViews.stream().filter(AdminThesisView::isCompleted).toList();

        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("thesisViews", thesisViews);
        model.addAttribute("activeThesisViews", activeThesisViews);
        model.addAttribute("completedThesisViews", completedThesisViews);
    }

    private void addOverviewMetrics(
            Model model,
            List<AdminUserApiItem> users,
            List<AdminTopicView> topicViews,
            List<AdminRegistrationView> registrationViews,
            List<AdminThesisView> thesisViews) {
        long activeUserCount = users.stream().filter(AdminUserApiItem::isActive).count();
        long inactiveUserCount = users.size() - activeUserCount;
        long openTopicCount = topicViews.stream().filter(AdminTopicView::isOpen).count();
        long closedTopicCount = topicViews.size() - openTopicCount;
        long pendingRegistrationCount = registrationViews.stream().filter(AdminRegistrationView::isPending).count();
        long approvedRegistrationCount = registrationViews.stream().filter(AdminRegistrationView::isApproved).count();
        long rejectedRegistrationCount = registrationViews.size() - pendingRegistrationCount - approvedRegistrationCount;
        long completedThesisCount = thesisViews.stream().filter(AdminThesisView::isCompleted).count();
        long activeThesisCount = thesisViews.size() - completedThesisCount;

        model.addAttribute("userActiveWidth", percent(activeUserCount, users.size()));
        model.addAttribute("userInactiveWidth", percent(inactiveUserCount, users.size()));
        model.addAttribute("topicOpenWidth", percent(openTopicCount, topicViews.size()));
        model.addAttribute("topicClosedWidth", percent(closedTopicCount, topicViews.size()));
        model.addAttribute("registrationPendingWidth", percent(pendingRegistrationCount, registrationViews.size()));
        model.addAttribute("registrationApprovedWidth", percent(approvedRegistrationCount, registrationViews.size()));
        model.addAttribute("registrationRejectedWidth", percent(rejectedRegistrationCount, registrationViews.size()));
        model.addAttribute("thesisActiveWidth", percent(activeThesisCount, thesisViews.size()));
        model.addAttribute("thesisCompletedWidth", percent(completedThesisCount, thesisViews.size()));
    }

    private List<AdminUserApiItem> sortedAcademicUsers(AdminUserCatalogView catalog) {
        return catalog.users().stream()
            .filter(this::isAcademicUser)
            .sorted(Comparator.comparing(AdminUserApiItem::getUsername, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private List<AdminTopicView> buildTopicViews(List<TheLoaiApiItem> topics) {
        Map<String, GiangVien> lecturersById = giangVienRepository.findAllById(
            topics.stream()
                .map(TheLoaiApiItem::getGvId)
                .filter(this::hasText)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(GiangVien::getGvId, Function.identity()));

        return topics.stream()
            .map(topic -> new AdminTopicView(topic, lecturersById.get(topic.getGvId())))
            .sorted(Comparator.comparing(
                (AdminTopicView view) -> view.topic().getNgayLap(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private List<AdminRegistrationView> buildRegistrationViews(List<DangKyApiItem> registrations, List<TheLoaiApiItem> topics) {
        Map<String, TheLoaiApiItem> topicsById = topics.stream()
            .filter(topic -> hasText(topic.getTlId()))
            .collect(Collectors.toMap(TheLoaiApiItem::getTlId, Function.identity(), (left, right) -> left));

        Map<String, SinhVien> studentsById = sinhVienRepository.findAllById(
            registrations.stream()
                .map(DangKyApiItem::getSvId)
                .filter(this::hasText)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(SinhVien::getSvId, Function.identity()));

        Map<String, GiangVien> approversById = giangVienRepository.findAllById(
            registrations.stream()
                .map(DangKyApiItem::getNguoiChapThuan)
                .filter(this::hasText)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(GiangVien::getGvId, Function.identity()));

        return registrations.stream()
            .map(registration -> new AdminRegistrationView(
                registration,
                topicsById.get(registration.getTlId()),
                studentsById.get(registration.getSvId()),
                approversById.get(registration.getNguoiChapThuan())))
            .sorted(Comparator.comparing(
                (AdminRegistrationView view) -> view.registration().getNgayDangKy(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private List<AdminThesisView> buildThesisViews(
            List<DoAnApiItem> theses,
            List<DangKyApiItem> registrations,
            List<TheLoaiApiItem> topics) {
        Map<String, DangKyApiItem> registrationsById = registrations.stream()
            .filter(registration -> hasText(registration.getDkId()))
            .collect(Collectors.toMap(DangKyApiItem::getDkId, Function.identity(), (left, right) -> left));
        Map<String, TheLoaiApiItem> topicsById = topics.stream()
            .filter(topic -> hasText(topic.getTlId()))
            .collect(Collectors.toMap(TheLoaiApiItem::getTlId, Function.identity(), (left, right) -> left));

        Map<String, SinhVien> studentsById = sinhVienRepository.findAllById(
            registrations.stream()
                .map(DangKyApiItem::getSvId)
                .filter(this::hasText)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(SinhVien::getSvId, Function.identity()));

        Map<String, GiangVien> lecturersById = giangVienRepository.findAllById(
            theses.stream()
                .map(DoAnApiItem::getGvId)
                .filter(this::hasText)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(GiangVien::getGvId, Function.identity()));

        return theses.stream()
            .map(thesis -> {
                DangKyApiItem registration = registrationsById.get(thesis.getDkId());
                TheLoaiApiItem topic = registration == null ? null : topicsById.get(registration.getTlId());
                SinhVien student = registration == null ? null : studentsById.get(registration.getSvId());
                GiangVien lecturer = lecturersById.get(thesis.getGvId());
                return new AdminThesisView(thesis, registration, topic, student, lecturer);
            })
            .sorted(Comparator.comparing(
                (AdminThesisView view) -> view.thesis().getNgayThucHien(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private String requireAdmin(HttpServletRequest request) {
        String username = authSessionService.getAuthenticatedUsername(request);
        if (!hasText(username)) {
            throw new IllegalArgumentException("Admin access only.");
        }

        if (demoUsername.equals(username)) {
            return username;
        }

        NguoiDung user = nguoiDungRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
        if (user != null && "AD".equalsIgnoreCase(user.getVaiTroId())) {
            return user.getUsername();
        }

        throw new IllegalArgumentException("Admin access only.");
    }

    private boolean isAcademicUser(AdminUserApiItem user) {
        if (user == null || user.getVaiTroId() == null) {
            return false;
        }

        String roleId = user.getVaiTroId().trim().toUpperCase(Locale.ROOT);
        return "SV".equals(roleId) || "GV".equals(roleId);
    }

    private void populateFormModel(Model model, AdminUserForm form, boolean createMode, String pageTitle) {
        model.addAttribute("form", form);
        model.addAttribute("createMode", createMode);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", createMode ? "/admin/users" : "/admin/users/" + form.getNdId());
        model.addAttribute("passwordLabel", createMode ? "Mat khau" : "Mat khau moi (de trong neu khong doi)");
        model.addAttribute("submitLabel", createMode ? "Tao tai khoan" : "Luu thay doi");
        model.addAttribute("activeSection", "dashboard");
    }

    private void normalizeAndValidateForm(AdminUserForm form, boolean createMode) {
        form.setUsername(trim(form.getUsername()));
        form.setEmail(trim(form.getEmail()));
        form.setVaiTroId(trimUpper(form.getVaiTroId()));
        form.setProfileName(trim(form.getProfileName()));
        form.setPassword(trim(form.getPassword()));

        requireField(form.getUsername(), "Username khong duoc de trong.");
        requireField(form.getEmail(), "Email khong duoc de trong.");

        if (createMode) {
            requireField(form.getVaiTroId(), "Vai tro khong duoc de trong.");
            if (!"SV".equals(form.getVaiTroId()) && !"GV".equals(form.getVaiTroId())) {
                throw new IllegalArgumentException("Chi duoc tao tai khoan voi vai tro SV hoac GV.");
            }
            if (form.getPassword() == null || form.getPassword().length() < 6) {
                throw new IllegalArgumentException("Mat khau phai co it nhat 6 ky tu.");
            }
        } else if (form.getPassword() != null && !form.getPassword().isBlank() && form.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mat khau phai co it nhat 6 ky tu.");
        }
    }

    private int percent(long value, int total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((value * 100.0) / total);
    }

    private void requireField(String value, String message) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimUpper(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private int calculateTotalPages(int itemCount, int pageSize) {
        if (itemCount <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) itemCount / pageSize);
    }

    private int clampPage(int requestedPage, int totalPages) {
        if (requestedPage < 1) {
            return 1;
        }
        return Math.min(requestedPage, totalPages);
    }

    private List<Integer> pageNumbers(int totalPages) {
        return java.util.stream.IntStream.rangeClosed(1, totalPages)
            .boxed()
            .toList();
    }

    private <T> List<T> pageItems(List<T> items, int currentPage, int pageSize) {
        int start = (currentPage - 1) * pageSize;
        if (start >= items.size()) {
            return List.of();
        }
        int end = Math.min(start + pageSize, items.size());
        return items.subList(start, end);
    }
}
