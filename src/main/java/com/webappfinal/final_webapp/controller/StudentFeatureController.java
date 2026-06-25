package com.webappfinal.final_webapp.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webappfinal.final_webapp.dto.BaiDangApiItem;
import com.webappfinal.final_webapp.dto.BaiDangCatalogView;
import com.webappfinal.final_webapp.dto.BaiDangForm;
import com.webappfinal.final_webapp.dto.DanhGiaApiItem;
import com.webappfinal.final_webapp.dto.DanhGiaCatalogView;
import com.webappfinal.final_webapp.dto.DangKyApiItem;
import com.webappfinal.final_webapp.dto.DangKyCatalogView;
import com.webappfinal.final_webapp.dto.DangKyForm;
import com.webappfinal.final_webapp.dto.DoAnApiItem;
import com.webappfinal.final_webapp.dto.DoAnCatalogView;
import com.webappfinal.final_webapp.dto.StudentEvaluationView;
import com.webappfinal.final_webapp.dto.StudentProgressView;
import com.webappfinal.final_webapp.dto.StudentRegistrationView;
import com.webappfinal.final_webapp.dto.StudentSubmissionView;
import com.webappfinal.final_webapp.dto.StudentThesisView;
import com.webappfinal.final_webapp.dto.TheLoaiApiItem;
import com.webappfinal.final_webapp.dto.TheLoaiCatalogView;
import com.webappfinal.final_webapp.dto.TienDoApiItem;
import com.webappfinal.final_webapp.dto.TienDoCatalogView;
import com.webappfinal.final_webapp.dto.TienDoForm;
import com.webappfinal.final_webapp.dto.TopicDetailView;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;
import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.BaiDangApiException;
import com.webappfinal.final_webapp.service.BaiDangApiService;
import com.webappfinal.final_webapp.service.DanhGiaApiService;
import com.webappfinal.final_webapp.service.DangKyApiException;
import com.webappfinal.final_webapp.service.DangKyApiService;
import com.webappfinal.final_webapp.service.DoAnApiService;
import com.webappfinal.final_webapp.service.TheLoaiApiService;
import com.webappfinal.final_webapp.service.TienDoApiException;
import com.webappfinal.final_webapp.service.TienDoApiService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class StudentFeatureController {
    private static final int TOPICS_PAGE_SIZE = 4;

    private final TheLoaiApiService theLoaiApiService;
    private final DangKyApiService dangKyApiService;
    private final DoAnApiService doAnApiService;
    private final TienDoApiService tienDoApiService;
    private final BaiDangApiService baiDangApiService;
    private final DanhGiaApiService danhGiaApiService;
    private final AuthSessionService authSessionService;
    private final NguoiDungRepository nguoiDungRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;

    public StudentFeatureController(
            TheLoaiApiService theLoaiApiService,
            DangKyApiService dangKyApiService,
            DoAnApiService doAnApiService,
            TienDoApiService tienDoApiService,
            BaiDangApiService baiDangApiService,
            DanhGiaApiService danhGiaApiService,
            AuthSessionService authSessionService,
            NguoiDungRepository nguoiDungRepository,
            SinhVienRepository sinhVienRepository,
            GiangVienRepository giangVienRepository) {
        this.theLoaiApiService = theLoaiApiService;
        this.dangKyApiService = dangKyApiService;
        this.doAnApiService = doAnApiService;
        this.tienDoApiService = tienDoApiService;
        this.baiDangApiService = baiDangApiService;
        this.danhGiaApiService = danhGiaApiService;
        this.authSessionService = authSessionService;
        this.nguoiDungRepository = nguoiDungRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.giangVienRepository = giangVienRepository;
    }

    @GetMapping("/student/topics")
    public String topics(
            @RequestParam(name = "openPage", defaultValue = "1") int openPage,
            @RequestParam(name = "closedPage", defaultValue = "1") int closedPage,
            Model model) {
        TheLoaiCatalogView catalog = theLoaiApiService.fetchTopics();
        List<TheLoaiApiItem> openTopics = catalog.topics().stream()
            .filter(topic -> topic.isOpen())
            .toList();
        List<TheLoaiApiItem> closedTopics = catalog.topics().stream()
            .filter(topic -> !topic.isOpen())
            .toList();
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
        return "student/topics";
    }

    @GetMapping("/student/topics/{tlId}")
    public String topicDetail(
            @PathVariable String tlId,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        TheLoaiApiItem topic = theLoaiApiService.fetchTopic(tlId).orElse(null);
        if (topic == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay chi tiet de tai tren Rails API.");
            return "redirect:/student/topics";
        }

        SinhVien student = requireStudent(request);
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForStudent(student.getSvId());
        DangKyApiItem existingRegistration = registrationCatalog.registrations().stream()
            .filter(registration -> tlId.equalsIgnoreCase(registration.getTlId()))
            .findFirst()
            .orElse(null);

        GiangVien lecturer = giangVienRepository.findById(topic.getGvId()).orElse(null);
        TopicDetailView detail = TopicDetailView.available(topic, lecturer);

        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new DangKyForm());
        }

        model.addAttribute("detail", detail);
        model.addAttribute("existingRegistration", existingRegistration);
        model.addAttribute("registrationCatalog", registrationCatalog);
        return "student/topic-detail";
    }

    @PostMapping("/student/topics/{tlId}/register")
    public String registerTopic(
            @PathVariable String tlId,
            @ModelAttribute("registrationForm") DangKyForm registrationForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);

        DangKyCatalogView existingCatalog = dangKyApiService.fetchRegistrationsForStudent(student.getSvId());
        boolean alreadyRegistered = existingCatalog.registrations().stream()
            .anyMatch(registration -> tlId.equalsIgnoreCase(registration.getTlId()));
        if (alreadyRegistered) {
            redirectAttributes.addFlashAttribute("info", "Ban da co dang ky cho de tai nay.");
            return "redirect:/student/topics/" + tlId;
        }

        try {
            dangKyApiService.createRegistration(student.getSvId(), tlId, registrationForm.getGhiChu());
            redirectAttributes.addFlashAttribute("success", "Dang ky de tai da duoc gui.");
            return "redirect:/student/my-thesis";
        } catch (DangKyApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("registrationForm", registrationForm);
            return "redirect:/student/topics/" + tlId;
        }
    }

    @GetMapping("/student/my-thesis")
    public String myThesis(HttpServletRequest request, Model model) {
        SinhVien student = requireStudent(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForStudent(student.getSvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForStudent(student.getSvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();

        Map<String, TheLoaiApiItem> topicsById = topicCatalog.topics().stream()
            .filter(topic -> topic.getTlId() != null)
            .collect(Collectors.toMap(TheLoaiApiItem::getTlId, Function.identity(), (left, right) -> left));
        Map<String, DangKyApiItem> registrationsById = registrationCatalog.registrations().stream()
            .filter(registration -> registration.getDkId() != null)
            .collect(Collectors.toMap(DangKyApiItem::getDkId, Function.identity(), (left, right) -> left));

        List<StudentThesisView> theses = thesisCatalog.theses().stream()
            .map(thesis -> toThesisView(thesis, registrationsById, topicsById))
            .sorted(Comparator.comparing(
                (StudentThesisView view) -> view.thesis().getNgayThucHien(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
        List<StudentThesisView> activeTheses = theses.stream()
            .filter(view -> !view.isCompleted())
            .toList();
        List<StudentThesisView> completedTheses = theses.stream()
            .filter(StudentThesisView::isCompleted)
            .toList();

        List<StudentRegistrationView> registrations = registrationCatalog.registrations().stream()
            .map(registration -> toRegistrationView(registration, topicsById))
            .sorted(Comparator.comparing(
                (StudentRegistrationView view) -> view.registration().getNgayDangKy(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        List<StudentRegistrationView> pendingRegistrations = registrations.stream()
            .filter(StudentRegistrationView::isPending)
            .toList();
        List<StudentRegistrationView> processedRegistrations = registrations.stream()
            .filter(view -> !view.isPending())
            .toList();

        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("theses", theses);
        model.addAttribute("activeTheses", activeTheses);
        model.addAttribute("completedTheses", completedTheses);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("registrations", registrations);
        model.addAttribute("pendingRegistrations", pendingRegistrations);
        model.addAttribute("processedRegistrations", processedRegistrations);
        model.addAttribute("student", student);
        return "student/my-thesis";
    }

    @GetMapping("/student/progress")
    public String progress(HttpServletRequest request, Model model) {
        SinhVien student = requireStudent(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForStudent(student.getSvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForStudent(student.getSvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();
        TienDoCatalogView progressCatalog = tienDoApiService.fetchProgressForStudent(student.getSvId());

        List<StudentThesisView> activeTheses = buildStudentThesisViews(thesisCatalog, registrationCatalog, topicCatalog).stream()
            .filter(view -> !view.isCompleted())
            .toList();
        List<StudentProgressView> progressViews = buildStudentProgressViews(activeTheses, progressCatalog.entries());

        model.addAttribute("student", student);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("progressCatalog", progressCatalog);
        model.addAttribute("progressViews", progressViews);
        return "student/progress";
    }

    @PostMapping("/student/progress/{daId}")
    public String createProgress(
            @PathVariable String daId,
            @ModelAttribute("progressForm") TienDoForm progressForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        if (findOwnedThesis(student, daId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay do an cua ban.");
            return "redirect:/student/progress";
        }

        try {
            tienDoApiService.createProgress(daId, progressForm);
            redirectAttributes.addFlashAttribute("success", "Tien do da duoc tao.");
        } catch (TienDoApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/progress";
    }

    @PostMapping("/student/progress/{tdId}/update")
    public String updateProgress(
            @PathVariable String tdId,
            @ModelAttribute("progressForm") TienDoForm progressForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        if (findOwnedProgress(student, tdId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay tien do cua ban.");
            return "redirect:/student/progress";
        }

        try {
            tienDoApiService.updateProgress(tdId, progressForm);
            redirectAttributes.addFlashAttribute("success", "Tien do da duoc cap nhat.");
        } catch (TienDoApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/progress";
    }

    @PostMapping("/student/progress/{tdId}/delete")
    public String deleteProgress(
            @PathVariable String tdId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        if (findOwnedProgress(student, tdId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay tien do cua ban.");
            return "redirect:/student/progress";
        }

        try {
            tienDoApiService.deleteProgress(tdId);
            redirectAttributes.addFlashAttribute("success", "Tien do da duoc xoa.");
        } catch (TienDoApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/progress";
    }

    @GetMapping("/student/submissions")
    public String submissions(HttpServletRequest request, Model model) {
        SinhVien student = requireStudent(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForStudent(student.getSvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForStudent(student.getSvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();
        BaiDangCatalogView submissionCatalog = baiDangApiService.fetchSubmissionsForStudent(student.getSvId());

        List<StudentThesisView> activeTheses = buildStudentThesisViews(thesisCatalog, registrationCatalog, topicCatalog).stream()
            .filter(view -> !view.isCompleted())
            .toList();
        List<StudentSubmissionView> submissionViews = buildStudentSubmissionViews(activeTheses, submissionCatalog.submissions());

        model.addAttribute("student", student);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("submissionCatalog", submissionCatalog);
        model.addAttribute("submissionViews", submissionViews);
        return "student/submissions";
    }

    @PostMapping("/student/submissions/{daId}")
    public String createSubmission(
            @PathVariable String daId,
            @ModelAttribute("submissionForm") BaiDangForm submissionForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        if (findOwnedThesis(student, daId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay do an cua ban.");
            return "redirect:/student/submissions";
        }

        try {
            baiDangApiService.createSubmission(daId, submissionForm);
            redirectAttributes.addFlashAttribute("success", "Bai nop da duoc tao.");
        } catch (BaiDangApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/submissions";
    }

    @PostMapping("/student/submissions/{bdId}/update")
    public String updateSubmission(
            @PathVariable String bdId,
            @ModelAttribute("submissionForm") BaiDangForm submissionForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        if (findOwnedSubmission(student, bdId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay bai nop cua ban.");
            return "redirect:/student/submissions";
        }

        try {
            baiDangApiService.updateSubmission(bdId, submissionForm);
            redirectAttributes.addFlashAttribute("success", "Bai nop da duoc cap nhat.");
        } catch (BaiDangApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/submissions";
    }

    @PostMapping("/student/submissions/{bdId}/delete")
    public String deleteSubmission(
            @PathVariable String bdId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        if (findOwnedSubmission(student, bdId) == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay bai nop cua ban.");
            return "redirect:/student/submissions";
        }

        try {
            baiDangApiService.deleteSubmission(bdId);
            redirectAttributes.addFlashAttribute("success", "Bai nop da duoc xoa.");
        } catch (BaiDangApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/submissions";
    }

    @GetMapping("/student/result")
    public String result(HttpServletRequest request, Model model) {
        SinhVien student = requireStudent(request);
        DoAnCatalogView thesisCatalog = doAnApiService.fetchThesesForStudent(student.getSvId());
        DangKyCatalogView registrationCatalog = dangKyApiService.fetchRegistrationsForStudent(student.getSvId());
        TheLoaiCatalogView topicCatalog = theLoaiApiService.fetchTopics();
        DanhGiaCatalogView evaluationCatalog = danhGiaApiService.fetchEvaluationsForStudent(student.getSvId());

        List<StudentThesisView> theses = buildStudentThesisViews(thesisCatalog, registrationCatalog, topicCatalog);
        Map<String, DanhGiaApiItem> evaluationsByThesis = evaluationCatalog.evaluations().stream()
            .filter(evaluation -> evaluation.getDaId() != null)
            .collect(Collectors.toMap(DanhGiaApiItem::getDaId, Function.identity(), (left, right) -> right, LinkedHashMap::new));

        List<StudentEvaluationView> evaluationViews = theses.stream()
            .map(thesis -> new StudentEvaluationView(thesis, evaluationsByThesis.get(thesis.thesis().getDaId())))
            .toList();

        model.addAttribute("student", student);
        model.addAttribute("thesisCatalog", thesisCatalog);
        model.addAttribute("registrationCatalog", registrationCatalog);
        model.addAttribute("topicCatalog", topicCatalog);
        model.addAttribute("evaluationCatalog", evaluationCatalog);
        model.addAttribute("evaluationViews", evaluationViews);
        return "student/result";
    }

    @PostMapping("/student/registrations/{dkId}/update")
    public String updateRegistration(
            @PathVariable String dkId,
            @ModelAttribute("registrationForm") DangKyForm registrationForm,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        DangKyApiItem registration = findOwnedRegistration(student, dkId);
        if (registration == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay dang ky cua ban.");
            return "redirect:/student/my-thesis";
        }

        try {
            dangKyApiService.updateRegistration(dkId, registrationForm.getGhiChu());
            redirectAttributes.addFlashAttribute("success", "Dang ky da duoc cap nhat.");
        } catch (DangKyApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/my-thesis";
    }

    @PostMapping("/student/registrations/{dkId}/delete")
    public String deleteRegistration(
            @PathVariable String dkId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        SinhVien student = requireStudent(request);
        DangKyApiItem registration = findOwnedRegistration(student, dkId);
        if (registration == null) {
            redirectAttributes.addFlashAttribute("info", "Khong tim thay dang ky cua ban.");
            return "redirect:/student/my-thesis";
        }

        try {
            dangKyApiService.deleteRegistration(dkId);
            redirectAttributes.addFlashAttribute("success", "Dang ky da duoc huy.");
        } catch (DangKyApiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/my-thesis";
    }

    private StudentThesisView toThesisView(
            DoAnApiItem thesis,
            Map<String, DangKyApiItem> registrationsById,
            Map<String, TheLoaiApiItem> topicsById) {
        DangKyApiItem registration = registrationsById.get(thesis.getDkId());
        TheLoaiApiItem topic = registration == null ? null : topicsById.get(registration.getTlId());
        GiangVien lecturer = giangVienRepository.findById(thesis.getGvId()).orElse(null);
        return new StudentThesisView(thesis, registration, topic, lecturer);
    }

    private StudentRegistrationView toRegistrationView(DangKyApiItem registration, Map<String, TheLoaiApiItem> topicsById) {
        TheLoaiApiItem topic = topicsById.get(registration.getTlId());
        GiangVien lecturer = topic == null ? null : giangVienRepository.findById(topic.getGvId()).orElse(null);
        return new StudentRegistrationView(registration, topic, lecturer);
    }

    private List<StudentThesisView> buildStudentThesisViews(
            DoAnCatalogView thesisCatalog,
            DangKyCatalogView registrationCatalog,
            TheLoaiCatalogView topicCatalog) {
        Map<String, DangKyApiItem> registrationsById = registrationCatalog.registrations().stream()
            .filter(registration -> registration.getDkId() != null)
            .collect(Collectors.toMap(DangKyApiItem::getDkId, Function.identity(), (left, right) -> left));
        Map<String, TheLoaiApiItem> topicsById = topicCatalog.topics().stream()
            .filter(topic -> topic.getTlId() != null)
            .collect(Collectors.toMap(TheLoaiApiItem::getTlId, Function.identity(), (left, right) -> left));

        return thesisCatalog.theses().stream()
            .map(thesis -> toThesisView(thesis, registrationsById, topicsById))
            .sorted(Comparator.comparing(
                (StudentThesisView view) -> view.thesis().getNgayThucHien(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private List<StudentProgressView> buildStudentProgressViews(List<StudentThesisView> theses, List<TienDoApiItem> entries) {
        Map<String, List<TienDoApiItem>> entriesByThesis = entries.stream()
            .filter(entry -> entry.getDaId() != null)
            .sorted(Comparator.comparing(TienDoApiItem::getNgayGui, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.groupingBy(TienDoApiItem::getDaId, LinkedHashMap::new, Collectors.toList()));

        return theses.stream()
            .map(thesis -> new StudentProgressView(thesis, entriesByThesis.getOrDefault(thesis.thesis().getDaId(), List.of())))
            .toList();
    }

    private List<StudentSubmissionView> buildStudentSubmissionViews(List<StudentThesisView> theses, List<BaiDangApiItem> submissions) {
        Map<String, List<BaiDangApiItem>> submissionsByThesis = submissions.stream()
            .filter(entry -> entry.getDaId() != null)
            .sorted(Comparator.comparing(BaiDangApiItem::getNgayDang, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.groupingBy(BaiDangApiItem::getDaId, LinkedHashMap::new, Collectors.toList()));

        return theses.stream()
            .map(thesis -> new StudentSubmissionView(thesis, submissionsByThesis.getOrDefault(thesis.thesis().getDaId(), List.of())))
            .toList();
    }

    private DangKyApiItem findOwnedRegistration(SinhVien student, String dkId) {
        return dangKyApiService.fetchRegistrationsForStudent(student.getSvId()).registrations().stream()
            .filter(registration -> dkId.equalsIgnoreCase(registration.getDkId()))
            .findFirst()
            .orElse(null);
    }

    private DoAnApiItem findOwnedThesis(SinhVien student, String daId) {
        return doAnApiService.fetchThesesForStudent(student.getSvId()).theses().stream()
            .filter(thesis -> daId.equalsIgnoreCase(thesis.getDaId()))
            .findFirst()
            .orElse(null);
    }

    private TienDoApiItem findOwnedProgress(SinhVien student, String tdId) {
        return tienDoApiService.fetchProgressForStudent(student.getSvId()).entries().stream()
            .filter(entry -> tdId.equalsIgnoreCase(entry.getTdId()))
            .findFirst()
            .orElse(null);
    }

    private BaiDangApiItem findOwnedSubmission(SinhVien student, String bdId) {
        return baiDangApiService.fetchSubmissionsForStudent(student.getSvId()).submissions().stream()
            .filter(entry -> bdId.equalsIgnoreCase(entry.getBdId()))
            .findFirst()
            .orElse(null);
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

    private SinhVien requireStudent(HttpServletRequest request) {
        String username = authSessionService.getAuthenticatedUsername(request);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("You need to log in first.");
        }

        NguoiDung user = nguoiDungRepository.findByUsernameIgnoreCase(username.trim())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return sinhVienRepository.findByNdId(user.getNdId())
            .orElseThrow(() -> new IllegalArgumentException("Student profile not found."));
    }
}
