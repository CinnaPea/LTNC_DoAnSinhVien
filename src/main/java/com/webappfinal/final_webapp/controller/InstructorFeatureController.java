package com.webappfinal.final_webapp.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webappfinal.final_webapp.dto.BaiDangApiItem;
import com.webappfinal.final_webapp.dto.BaiDangCatalogView;
import com.webappfinal.final_webapp.dto.DanhGiaApiItem;
import com.webappfinal.final_webapp.dto.DanhGiaCatalogView;
import com.webappfinal.final_webapp.dto.DanhGiaForm;
import com.webappfinal.final_webapp.dto.DangKyApiItem;
import com.webappfinal.final_webapp.dto.DangKyCatalogView;
import com.webappfinal.final_webapp.dto.DoAnApiItem;
import com.webappfinal.final_webapp.dto.DoAnCatalogView;
import com.webappfinal.final_webapp.dto.InstructorEvaluationView;
import com.webappfinal.final_webapp.dto.InstructorProgressView;
import com.webappfinal.final_webapp.dto.InstructorRegistrationView;
import com.webappfinal.final_webapp.dto.InstructorSubmissionView;
import com.webappfinal.final_webapp.dto.InstructorThesisView;
import com.webappfinal.final_webapp.dto.TheLoaiApiItem;
import com.webappfinal.final_webapp.dto.TheLoaiCatalogView;
import com.webappfinal.final_webapp.dto.TheLoaiForm;
import com.webappfinal.final_webapp.dto.TienDoApiItem;
import com.webappfinal.final_webapp.dto.TienDoCatalogView;
import com.webappfinal.final_webapp.dto.TienDoForm;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;
import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.BaiDangApiService;
import com.webappfinal.final_webapp.service.DanhGiaApiException;
import com.webappfinal.final_webapp.service.DanhGiaApiService;
import com.webappfinal.final_webapp.service.DangKyApiException;
import com.webappfinal.final_webapp.service.DangKyApiService;
import com.webappfinal.final_webapp.service.DoAnApiService;
import com.webappfinal.final_webapp.service.TheLoaiApiException;
import com.webappfinal.final_webapp.service.TheLoaiApiService;
import com.webappfinal.final_webapp.service.TienDoApiException;
import com.webappfinal.final_webapp.service.TienDoApiService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/instructor")
public class InstructorFeatureController {
    private static final int TOPICS_PAGE_SIZE = 4;

    private final TheLoaiApiService theLoaiApiService;
    private final DangKyApiService dangKyApiService;
    private final DoAnApiService doAnApiService;
    private final TienDoApiService tienDoApiService;
    private final BaiDangApiService baiDangApiService;
    private final DanhGiaApiService danhGiaApiService;
    private final AuthSessionService authSessionService;
    private final NguoiDungRepository nguoiDungRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;

    public InstructorFeatureController(
            TheLoaiApiService theLoaiApiService,
            DangKyApiService dangKyApiService,
            DoAnApiService doAnApiService,
            TienDoApiService tienDoApiService,
            BaiDangApiService baiDangApiService,
            DanhGiaApiService danhGiaApiService,
            AuthSessionService authSessionService,
            NguoiDungRepository nguoiDungRepository,
            GiangVienRepository giangVienRepository,
            SinhVienRepository sinhVienRepository) {
        this.theLoaiApiService = theLoaiApiService;
        this.dangKyApiService = dangKyApiService;
        this.doAnApiService = doAnApiService;
        this.tienDoApiService = tienDoApiService;
        this.baiDangApiService = baiDangApiService;
        this.danhGiaApiService = danhGiaApiService;
        this.authSessionService = authSessionService;
        this.nguoiDungRepository = nguoiDungRepository;
        this.giangVienRepository = giangVienRepository;
        this.sinhVienRepository = sinhVienRepository;
    }

    @GetMapping("/topics")
    public String topics(
            @RequestParam(name = "openPage", defaultValue = "1") int openPage,
            @RequestParam(name = "closedPage", defaultValue = "1") int closedPage,
            HttpServletRequest request,
            Model model) {
        GiangVien lecturer = requireLecturer(request);
        TheLoaiCatalogView catalog = theLoaiApiService.fetchTopicsForLecturer(lecturer.getGvId());
        List<TheLoaiApiItem> openTopics = catalog.topics().stream().filter(TheLoaiApiItem::isOpen).toList();
        List<TheLoaiApiItem> closedTopics = catalog.topics().stream().filter(topic -> !topic.isOpen()).toList();
        int openTotalPages = calculateTotalPages(openTopics.size(), TOPICS_PAGE_SIZE);
        int closedTotalPages = calculateTotalPages(closedTopics.size(), TOPICS_PAGE_SIZE);
        int safeOpenPage = clampPage(openPage, openTotalPages);
        int safeClosedPage = clampPage(closedPage, closedTotalPages);

        model.addAttribute("catalog", catalog);
        model.addAttribute("topics", catalog.topics());
        model.addAttribute("openTopics", openTopics);
        model.addAttribute("closedTopics", closedTopics);
        model.addAttribute("pagedOpenTopics", pageItems(openTopics, safeOpenPage, TOPICS_PAGE_SIZE));
        model.addAttribute("pagedClosedTopics", pageItems(closedTopics, safeClosedPage, TOPICS_PAGE_SIZE));
        model.addAttribute("openCurrentPage", safeOpenPage);
        model.addAttribute("openTotalPages", openTotalPages);
        model.addAttribute("openPageNumbers", pageNumbers(openTotalPages));
        model.addAttribute("closedCurrentPage", safeClosedPage);
        model.addAttribute("closedTotalPages", closedTotalPages);
        model.addAttribute("closedPageNumbers", pageNumbers(closedTotalPages));
        model.addAttribute("lecturer", lecturer);
        return "instructor/topics";
    }

    @GetMapping("/topics/new")
    public String newTopic(HttpServletRequest request, Model model) {
        GiangVien lecturer = requireLecturer(request);
        if (!model.containsAttribute("topicForm")) {
            model.addAttribute("topicForm", new TheLoaiForm());
        }
        populateTopicFormPage(model, lecturer, true, null);
        return "instructor/topic-form";
    }

    @PostMapping("/topics")
    public String createTopic(
            @ModelAttribute("topicForm") TheLoaiForm topicForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        try {
            theLoaiApiService.createTopic(topicForm, lecturer.getGvId());
            redirectAttributes.addFlashAttribute("success", "De tai moi da duoc tao.");
            return "redirect:/instructor/topics";
        } catch (TheLoaiApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("topicForm", topicForm);
            return "redirect:/instructor/topics/new";
        }
    }

    @GetMapping("/topics/{tlId}/edit")
    public String editTopic(@PathVariable String tlId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        TheLoaiApiItem topic = theLoaiApiService.fetchTopic(tlId).orElse(null);
        if (topic == null || !lecturer.getGvId().equals(topic.getGvId())) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay de tai thuoc quyen quan ly cua ban.");
            return "redirect:/instructor/topics";
        }

        if (!model.containsAttribute("topicForm")) {
            model.addAttribute("topicForm", toForm(topic));
        }
        populateTopicFormPage(model, lecturer, false, tlId);
        return "instructor/topic-form";
    }

    @PostMapping("/topics/{tlId}")
    public String updateTopic(
            @PathVariable String tlId,
            @ModelAttribute("topicForm") TheLoaiForm topicForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        TheLoaiApiItem topic = theLoaiApiService.fetchTopic(tlId).orElse(null);
        if (topic == null || !lecturer.getGvId().equals(topic.getGvId())) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay de tai thuoc quyen quan ly cua ban.");
            return "redirect:/instructor/topics";
        }

        try {
            theLoaiApiService.updateTopic(tlId, topicForm, lecturer.getGvId());
            redirectAttributes.addFlashAttribute("success", "De tai da duoc cap nhat.");
            return "redirect:/instructor/topics";
        } catch (TheLoaiApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("topicForm", topicForm);
            return "redirect:/instructor/topics/" + tlId + "/edit";
        }
    }

    @PostMapping("/topics/{tlId}/delete")
    public String deleteTopic(@PathVariable String tlId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        TheLoaiApiItem topic = theLoaiApiService.fetchTopic(tlId).orElse(null);
        if (topic == null || !lecturer.getGvId().equals(topic.getGvId())) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay de tai thuoc quyen quan ly cua ban.");
            return "redirect:/instructor/topics";
        }

        try {
            theLoaiApiService.deleteTopic(tlId);
            redirectAttributes.addFlashAttribute("success", "De tai da duoc xoa.");
        } catch (TheLoaiApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/instructor/topics";
    }

    @GetMapping("/students")
    public String students(HttpServletRequest request, Model model) {
        GiangVien lecturer = requireLecturer(request);
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForLecturer(lecturer.getGvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopicsForLecturer(lecturer.getGvId());

        Map<String, TheLoaiApiItem> topicsById = topicCatalog.topics().stream()
            .filter(topic -> topic.getTlId() != null)
            .collect(Collectors.toMap(TheLoaiApiItem::getTlId, Function.identity(), (left, right) -> left));

        Map<String, SinhVien> studentsById = sinhVienRepository.findAllById(
            registrationCatalog.registrations().stream()
                .map(DangKyApiItem::getSvId)
                .filter(svId -> svId != null && !svId.isBlank())
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(SinhVien::getSvId, Function.identity()));

        List<InstructorRegistrationView> registrations = registrationCatalog.registrations().stream()
            .map(registration -> toRegistrationView(registration, topicsById, studentsById))
            .sorted(Comparator.comparing(
                (InstructorRegistrationView view) -> view.registration().getNgayDangKy(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        List<InstructorRegistrationView> pendingRegistrations = registrations.stream()
            .filter(InstructorRegistrationView::isPending)
            .toList();
        List<InstructorRegistrationView> processedRegistrations = registrations.stream()
            .filter(view -> !view.isPending())
            .toList();

        model.addAttribute("lecturer", lecturer);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("registrations", registrations);
        model.addAttribute("pendingRegistrations", pendingRegistrations);
        model.addAttribute("processedRegistrations", processedRegistrations);
        return "instructor/students";
    }

    @GetMapping("/thesis")
    public String thesis(HttpServletRequest request, Model model) {
        GiangVien lecturer = requireLecturer(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForLecturer(lecturer.getGvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForLecturer(lecturer.getGvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopicsForLecturer(lecturer.getGvId());

        Map<String, DangKyApiItem> registrationsById = registrationCatalog.registrations().stream()
            .filter(registration -> registration.getDkId() != null)
            .collect(Collectors.toMap(DangKyApiItem::getDkId, Function.identity(), (left, right) -> left));
        Map<String, TheLoaiApiItem> topicsById = topicCatalog.topics().stream()
            .filter(topic -> topic.getTlId() != null)
            .collect(Collectors.toMap(TheLoaiApiItem::getTlId, Function.identity(), (left, right) -> left));

        Map<String, SinhVien> studentsById = sinhVienRepository.findAllById(
            registrationCatalog.registrations().stream()
                .map(DangKyApiItem::getSvId)
                .filter(svId -> svId != null && !svId.isBlank())
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(SinhVien::getSvId, Function.identity()));

        List<InstructorThesisView> theses = thesisCatalog.theses().stream()
            .map(thesis -> toThesisView(thesis, registrationsById, topicsById, studentsById))
            .sorted(Comparator.comparing(
                (InstructorThesisView view) -> view.thesis().getNgayThucHien(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
        List<InstructorThesisView> activeTheses = theses.stream()
            .filter(view -> !view.isCompleted())
            .toList();
        List<InstructorThesisView> completedTheses = theses.stream()
            .filter(InstructorThesisView::isCompleted)
            .toList();

        model.addAttribute("lecturer", lecturer);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("theses", theses);
        model.addAttribute("activeTheses", activeTheses);
        model.addAttribute("completedTheses", completedTheses);
        return "instructor/thesis";
    }

    @GetMapping("/progress")
    public String progress(HttpServletRequest request, Model model) {
        GiangVien lecturer = requireLecturer(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForLecturer(lecturer.getGvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForLecturer(lecturer.getGvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopicsForLecturer(lecturer.getGvId());
        TienDoCatalogView progressCatalog = tienDoApiService.fetchProgressForLecturer(lecturer.getGvId());

        List<InstructorThesisView> theses = buildInstructorThesisViews(thesisCatalog, registrationCatalog, topicCatalog);
        List<InstructorProgressView> progressViews = buildInstructorProgressViews(theses, progressCatalog.entries());

        model.addAttribute("lecturer", lecturer);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("progressCatalog", progressCatalog);
        model.addAttribute("progressViews", progressViews);
        return "instructor/progress";
    }

    @PostMapping("/progress/{tdId}/feedback")
    public String updateProgressFeedback(
            @PathVariable String tdId,
            @ModelAttribute("progressForm") TienDoForm progressForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        if (findOwnedProgress(lecturer, tdId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay tien do thuoc quyen xem cua ban.");
            return "redirect:/instructor/progress";
        }

        try {
            tienDoApiService.updateFeedback(tdId, progressForm.getNhanXet());
            redirectAttributes.addFlashAttribute("success", "Nhan xet tien do da duoc cap nhat.");
        } catch (TienDoApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/instructor/progress";
    }

    @GetMapping("/submissions")
    public String submissions(HttpServletRequest request, Model model) {
        GiangVien lecturer = requireLecturer(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForLecturer(lecturer.getGvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForLecturer(lecturer.getGvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopicsForLecturer(lecturer.getGvId());
        BaiDangCatalogView submissionCatalog = baiDangApiService.fetchSubmissionsForLecturer(lecturer.getGvId());

        List<InstructorThesisView> theses = buildInstructorThesisViews(thesisCatalog, registrationCatalog, topicCatalog);
        List<InstructorSubmissionView> submissionViews = buildInstructorSubmissionViews(theses, submissionCatalog.submissions());

        model.addAttribute("lecturer", lecturer);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("submissionCatalog", submissionCatalog);
        model.addAttribute("submissionViews", submissionViews);
        return "instructor/submissions";
    }

    @GetMapping("/evaluation")
    public String evaluation(HttpServletRequest request, Model model) {
        GiangVien lecturer = requireLecturer(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForLecturer(lecturer.getGvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForLecturer(lecturer.getGvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopicsForLecturer(lecturer.getGvId());
        DanhGiaCatalogView evaluationCatalog = danhGiaApiService.fetchEvaluationsForLecturer(lecturer.getGvId());

        List<InstructorThesisView> theses = buildInstructorThesisViews(thesisCatalog, registrationCatalog, topicCatalog);
        Map<String, DanhGiaApiItem> evaluationsByThesis = evaluationCatalog.evaluations().stream()
            .filter(evaluation -> evaluation.getDaId() != null)
            .collect(Collectors.toMap(DanhGiaApiItem::getDaId, Function.identity(), (left, right) -> right, LinkedHashMap::new));

        List<InstructorEvaluationView> evaluationViews = theses.stream()
            .map(thesis -> new InstructorEvaluationView(thesis, evaluationsByThesis.get(thesis.thesis().getDaId())))
            .toList();

        model.addAttribute("lecturer", lecturer);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("evaluationCatalog", evaluationCatalog);
        model.addAttribute("evaluationViews", evaluationViews);
        return "instructor/evaluation";
    }

    @PostMapping("/evaluation/{daId}")
    public String createEvaluation(
            @PathVariable String daId,
            @ModelAttribute("evaluationForm") DanhGiaForm evaluationForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        if (findSupervisedThesis(lecturer, daId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay do an thuoc quyen huong dan cua ban.");
            return "redirect:/instructor/evaluation";
        }

        try {
            danhGiaApiService.createEvaluation(daId, lecturer.getGvId(), evaluationForm);
            redirectAttributes.addFlashAttribute("success", "Danh gia da duoc tao.");
        } catch (DanhGiaApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/instructor/evaluation";
    }

    @PostMapping("/evaluation/{dgId}/update")
    public String updateEvaluation(
            @PathVariable String dgId,
            @ModelAttribute("evaluationForm") DanhGiaForm evaluationForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        if (findOwnedEvaluation(lecturer, dgId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay danh gia cua ban.");
            return "redirect:/instructor/evaluation";
        }

        try {
            danhGiaApiService.updateEvaluation(dgId, evaluationForm);
            redirectAttributes.addFlashAttribute("success", "Danh gia da duoc cap nhat.");
        } catch (DanhGiaApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/instructor/evaluation";
    }

    @PostMapping("/evaluation/{dgId}/delete")
    public String deleteEvaluation(
            @PathVariable String dgId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        if (findOwnedEvaluation(lecturer, dgId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay danh gia cua ban.");
            return "redirect:/instructor/evaluation";
        }

        try {
            danhGiaApiService.deleteEvaluation(dgId);
            redirectAttributes.addFlashAttribute("success", "Danh gia da duoc xoa.");
        } catch (DanhGiaApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/instructor/evaluation";
    }

    @PostMapping("/students/{dkId}/approve")
    public String approve(@PathVariable String dkId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        DangKyApiItem registration = findOwnedRegistration(lecturer, dkId);
        if (registration == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay dang ky thuoc quyen xu ly cua ban.");
            return "redirect:/instructor/students";
        }

        try {
            dangKyApiService.approveRegistration(dkId, lecturer.getGvId());
            redirectAttributes.addFlashAttribute("success", "Dang ky da duoc duyet.");
        } catch (DangKyApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/instructor/students";
    }

    @PostMapping("/students/{dkId}/reject")
    public String reject(@PathVariable String dkId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        GiangVien lecturer = requireLecturer(request);
        DangKyApiItem registration = findOwnedRegistration(lecturer, dkId);
        if (registration == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay dang ky thuoc quyen xu ly cua ban.");
            return "redirect:/instructor/students";
        }

        try {
            dangKyApiService.rejectRegistration(dkId, lecturer.getGvId());
            redirectAttributes.addFlashAttribute("success", "Dang ky da duoc tu choi.");
        } catch (DangKyApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/instructor/students";
    }

    private GiangVien requireLecturer(HttpServletRequest request) {
        String username = authSessionService.getAuthenticatedUsername(request);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("You need to log in first.");
        }

        NguoiDung user = nguoiDungRepository.findByUsernameIgnoreCase(username.trim())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return giangVienRepository.findByNdId(user.getNdId())
            .orElseThrow(() -> new IllegalArgumentException("Lecturer profile not found."));
    }

    private void populateTopicFormPage(Model model, GiangVien lecturer, boolean createMode, String tlId) {
        model.addAttribute("lecturer", lecturer);
        model.addAttribute("createMode", createMode);
        model.addAttribute("formAction", createMode ? "/instructor/topics" : "/instructor/topics/" + tlId);
    }

    private InstructorRegistrationView toRegistrationView(
            DangKyApiItem registration,
            Map<String, TheLoaiApiItem> topicsById,
            Map<String, SinhVien> studentsById) {
        TheLoaiApiItem topic = topicsById.get(registration.getTlId());
        SinhVien student = studentsById.get(registration.getSvId());
        return new InstructorRegistrationView(registration, topic, student);
    }

    private DangKyApiItem findOwnedRegistration(GiangVien lecturer, String dkId) {
        return dangKyApiService.fetchRegistrationsForLecturer(lecturer.getGvId()).registrations().stream()
            .filter(registration -> dkId.equalsIgnoreCase(registration.getDkId()))
            .findFirst()
            .orElse(null);
    }

    private List<InstructorThesisView> buildInstructorThesisViews(
            DoAnCatalogView thesisCatalog,
            DangKyCatalogView registrationCatalog,
            TheLoaiCatalogView topicCatalog) {
        Map<String, DangKyApiItem> registrationsById = registrationCatalog.registrations().stream()
            .filter(registration -> registration.getDkId() != null)
            .collect(Collectors.toMap(DangKyApiItem::getDkId, Function.identity(), (left, right) -> left));
        Map<String, TheLoaiApiItem> topicsById = topicCatalog.topics().stream()
            .filter(topic -> topic.getTlId() != null)
            .collect(Collectors.toMap(TheLoaiApiItem::getTlId, Function.identity(), (left, right) -> left));

        Map<String, SinhVien> studentsById = sinhVienRepository.findAllById(
            registrationCatalog.registrations().stream()
                .map(DangKyApiItem::getSvId)
                .filter(svId -> svId != null && !svId.isBlank())
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(SinhVien::getSvId, Function.identity()));

        return thesisCatalog.theses().stream()
            .map(thesis -> toThesisView(thesis, registrationsById, topicsById, studentsById))
            .sorted(Comparator.comparing(
                (InstructorThesisView view) -> view.thesis().getNgayThucHien(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private List<InstructorProgressView> buildInstructorProgressViews(List<InstructorThesisView> theses, List<TienDoApiItem> entries) {
        Map<String, List<TienDoApiItem>> entriesByThesis = entries.stream()
            .filter(entry -> entry.getDaId() != null)
            .sorted(Comparator.comparing(TienDoApiItem::getNgayGui, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.groupingBy(TienDoApiItem::getDaId, LinkedHashMap::new, Collectors.toList()));

        return theses.stream()
            .map(thesis -> new InstructorProgressView(thesis, entriesByThesis.getOrDefault(thesis.thesis().getDaId(), List.of())))
            .toList();
    }

    private List<InstructorSubmissionView> buildInstructorSubmissionViews(List<InstructorThesisView> theses, List<BaiDangApiItem> submissions) {
        Map<String, List<BaiDangApiItem>> submissionsByThesis = submissions.stream()
            .filter(entry -> entry.getDaId() != null)
            .sorted(Comparator.comparing(BaiDangApiItem::getNgayDang, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.groupingBy(BaiDangApiItem::getDaId, LinkedHashMap::new, Collectors.toList()));

        return theses.stream()
            .map(thesis -> new InstructorSubmissionView(thesis, submissionsByThesis.getOrDefault(thesis.thesis().getDaId(), List.of())))
            .toList();
    }

    private DoAnApiItem findSupervisedThesis(GiangVien lecturer, String daId) {
        return doAnApiService.fetchThesesForLecturer(lecturer.getGvId()).theses().stream()
            .filter(thesis -> daId.equalsIgnoreCase(thesis.getDaId()))
            .findFirst()
            .orElse(null);
    }

    private TienDoApiItem findOwnedProgress(GiangVien lecturer, String tdId) {
        return tienDoApiService.fetchProgressForLecturer(lecturer.getGvId()).entries().stream()
            .filter(entry -> tdId.equalsIgnoreCase(entry.getTdId()))
            .findFirst()
            .orElse(null);
    }

    private DanhGiaApiItem findOwnedEvaluation(GiangVien lecturer, String dgId) {
        return danhGiaApiService.fetchEvaluationsForLecturer(lecturer.getGvId()).evaluations().stream()
            .filter(entry -> dgId.equalsIgnoreCase(entry.getDgId()))
            .findFirst()
            .orElse(null);
    }

    private InstructorThesisView toThesisView(
            DoAnApiItem thesis,
            Map<String, DangKyApiItem> registrationsById,
            Map<String, TheLoaiApiItem> topicsById,
            Map<String, SinhVien> studentsById) {
        DangKyApiItem registration = registrationsById.get(thesis.getDkId());
        TheLoaiApiItem topic = registration == null ? null : topicsById.get(registration.getTlId());
        SinhVien student = registration == null ? null : studentsById.get(registration.getSvId());
        return new InstructorThesisView(thesis, registration, topic, student);
    }

    private TheLoaiForm toForm(TheLoaiApiItem topic) {
        TheLoaiForm form = new TheLoaiForm();
        form.setTlId(topic.getTlId());
        form.setMaTl(topic.getMaTl());
        form.setTenTl(topic.getTenTl());
        form.setMoTa(topic.getMoTa());
        form.setTrangThai(topic.getTrangThai());
        return form;
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
