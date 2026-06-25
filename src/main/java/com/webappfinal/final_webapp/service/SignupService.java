package com.webappfinal.final_webapp.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webappfinal.final_webapp.dto.SignupForm;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;
import com.webappfinal.final_webapp.repository.VaiTroRepository;

@Service
public class SignupService {
    private static final String ACCOUNT_TYPE_STUDENT = "SV";
    private static final String ACCOUNT_TYPE_LECTURER = "GV";
    private static final int MAX_ID_RETRIES = 20;

    private final NguoiDungRepository nguoiDungRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final VaiTroRepository vaiTroRepository;
    private final IdGenerationService idGenerationService;
    private final PasswordEncoder passwordEncoder;

    public SignupService(
            NguoiDungRepository nguoiDungRepository,
            SinhVienRepository sinhVienRepository,
            GiangVienRepository giangVienRepository,
            VaiTroRepository vaiTroRepository,
            IdGenerationService idGenerationService,
            PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.giangVienRepository = giangVienRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.idGenerationService = idGenerationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(SignupForm rawForm) {
        SignupForm form = normalize(rawForm);
        validate(form);

        String ndId = allocateAvailableNguoiDungId();

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setNdId(ndId);
        nguoiDung.setUsername(form.getUsername());
        nguoiDung.setPassHash(passwordEncoder.encode(form.getPassword()));
        nguoiDung.setEmail(form.getEmail());
        nguoiDung.setVaiTroId(form.getAccountType());
        nguoiDung.setTrangThai(true);
        nguoiDung.setNgayLap(LocalDateTime.now());
        nguoiDungRepository.saveAndFlush(nguoiDung);

        if (ACCOUNT_TYPE_STUDENT.equals(form.getAccountType())) {
            SinhVien sinhVien = sinhVienRepository.findByNdId(ndId)
                .orElseThrow(() -> new IllegalStateException("Student profile was not created by the database trigger."));

            sinhVien.setHoTen(form.getHoTen());
            sinhVien.setTenLop(form.getTenLop());
            sinhVien.setChuyenNganh(form.getChuyenNganh());
            sinhVien.setNienKhoa(form.getNienKhoa());
            sinhVienRepository.save(sinhVien);
            return;
        }

        GiangVien giangVien = giangVienRepository.findByNdId(ndId)
            .orElseThrow(() -> new IllegalStateException("Lecturer profile was not created by the database trigger."));

        giangVien.setHoTen(form.getHoTen());
        giangVien.setThuocVien(form.getThuocVien());
        giangVien.setHocVi(form.getHocVi());
        giangVienRepository.save(giangVien);
    }

    private void validate(SignupForm form) {
        requireField(form.getUsername(), "Username is required.");
        requireField(form.getEmail(), "Email is required.");
        requireField(form.getHoTen(), "Full name is required.");
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
        if (!ACCOUNT_TYPE_STUDENT.equals(form.getAccountType()) && !ACCOUNT_TYPE_LECTURER.equals(form.getAccountType())) {
            throw new IllegalArgumentException("Unsupported account type.");
        }
        if (!vaiTroRepository.existsById(form.getAccountType())) {
            throw new IllegalArgumentException("Selected role does not exist in the database.");
        }
        if (nguoiDungRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (nguoiDungRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        if (ACCOUNT_TYPE_STUDENT.equals(form.getAccountType())) {
            requireField(form.getTenLop(), "Class is required.");
            requireField(form.getChuyenNganh(), "Major is required.");
            requireField(form.getNienKhoa(), "School year is required.");
        } else {
            requireField(form.getThuocVien(), "Institute is required.");
            requireField(form.getHocVi(), "Degree is required.");
        }
    }

    private void requireField(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String allocateAvailableNguoiDungId() {
        for (int attempt = 0; attempt < MAX_ID_RETRIES; attempt++) {
            String candidate = idGenerationService.allocateNextId(IdGenerationService.NGUOI_DUNG);
            if (nguoiDungRepository.existsById(candidate)) {
                continue;
            }
            if (sinhVienRepository.findByNdId(candidate).isPresent()) {
                continue;
            }
            if (giangVienRepository.findByNdId(candidate).isPresent()) {
                continue;
            }
            return candidate;
        }
        throw new IllegalStateException("Unable to allocate a free ND_ID after " + MAX_ID_RETRIES + " attempts.");
    }

    private SignupForm normalize(SignupForm rawForm) {
        SignupForm normalized = new SignupForm();
        normalized.setAccountType(trimUpper(rawForm.getAccountType()));
        normalized.setUsername(trim(rawForm.getUsername()));
        normalized.setPassword(rawForm.getPassword());
        normalized.setConfirmPassword(rawForm.getConfirmPassword());
        normalized.setEmail(trim(rawForm.getEmail()));
        normalized.setHoTen(trim(rawForm.getHoTen()));
        normalized.setTenLop(trim(rawForm.getTenLop()));
        normalized.setChuyenNganh(trim(rawForm.getChuyenNganh()));
        normalized.setNienKhoa(trim(rawForm.getNienKhoa()));
        normalized.setThuocVien(trim(rawForm.getThuocVien()));
        normalized.setHocVi(trim(rawForm.getHocVi()));
        return normalized;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimUpper(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
