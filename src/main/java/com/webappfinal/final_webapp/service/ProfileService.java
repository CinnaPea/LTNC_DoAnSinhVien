package com.webappfinal.final_webapp.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webappfinal.final_webapp.dto.UserProfileForm;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;

import jakarta.persistence.EntityManager;

@Service
public class ProfileService {
    private final EntityManager entityManager;
    private final GiangVienRepository giangVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SinhVienRepository sinhVienRepository;

    @Autowired
    public ProfileService(
            EntityManager entityManager,
            GiangVienRepository giangVienRepository,
            NguoiDungRepository nguoiDungRepository,
            SinhVienRepository sinhVienRepository) {
        this.entityManager = entityManager;
        this.giangVienRepository = giangVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.sinhVienRepository = sinhVienRepository;
    }

    public ProfileService(
            GiangVienRepository giangVienRepository,
            NguoiDungRepository nguoiDungRepository,
            SinhVienRepository sinhVienRepository) {
        this(null, giangVienRepository, nguoiDungRepository, sinhVienRepository);
    }

    @Transactional(readOnly = true)
    public UserProfileForm getStudentProfile(String username) {
        NguoiDung nguoiDung = findNguoiDung(username);
        SinhVien sinhVien = sinhVienRepository.findByNdId(nguoiDung.getNdId())
            .orElseThrow(() -> new IllegalArgumentException("Student profile not found."));

        UserProfileForm form = new UserProfileForm();
        form.setLoaiTaiKhoan("SV");
        form.setUsername(nguoiDung.getUsername());
        form.setProfileCode(sinhVien.getMaSv());
        form.setHoTen(sinhVien.getHoTen());
        form.setEmail(nguoiDung.getEmail());
        form.setTenLop(sinhVien.getTenLop());
        form.setChuyenNganh(sinhVien.getChuyenNganh());
        form.setNienKhoa(sinhVien.getNienKhoa());
        return form;
    }

    @Transactional(readOnly = true)
    public UserProfileForm getInstructorProfile(String username) {
        NguoiDung nguoiDung = findNguoiDung(username);
        GiangVien giangVien = giangVienRepository.findByNdId(nguoiDung.getNdId())
            .orElseThrow(() -> new IllegalArgumentException("Lecturer profile not found."));

        UserProfileForm form = new UserProfileForm();
        form.setLoaiTaiKhoan("GV");
        form.setUsername(nguoiDung.getUsername());
        form.setProfileCode(giangVien.getMaGv());
        form.setHoTen(giangVien.getHoTen());
        form.setEmail(nguoiDung.getEmail());
        form.setThuocVien(giangVien.getThuocVien());
        form.setHocVi(giangVien.getHocVi());
        return form;
    }

    @Transactional
    public void updateStudentProfile(String username, UserProfileForm rawForm) {
        NguoiDung nguoiDung = findNguoiDung(username);
        SinhVien sinhVien = sinhVienRepository.findByNdId(nguoiDung.getNdId())
            .orElseThrow(() -> new IllegalArgumentException("Student profile not found."));
        UserProfileForm form = normalize(rawForm);

        require(form.getHoTen(), "Full name is required.");
        require(form.getEmail(), "Email is required.");
        require(form.getTenLop(), "Class is required.");
        require(form.getChuyenNganh(), "Major is required.");
        require(form.getNienKhoa(), "School year is required.");
        ensureEmailAvailable(form.getEmail(), nguoiDung.getNdId());

        nguoiDung.setEmail(form.getEmail());
        nguoiDung.setCapNhat(LocalDateTime.now());
        sinhVien.setHoTen(form.getHoTen());
        sinhVien.setTenLop(form.getTenLop());
        sinhVien.setChuyenNganh(form.getChuyenNganh());
        sinhVien.setNienKhoa(form.getNienKhoa());

        nguoiDungRepository.save(nguoiDung);
        sinhVienRepository.save(sinhVien);
    }

    @Transactional
    public void updateInstructorProfile(String username, UserProfileForm rawForm) {
        NguoiDung nguoiDung = findNguoiDung(username);
        GiangVien giangVien = giangVienRepository.findByNdId(nguoiDung.getNdId())
            .orElseThrow(() -> new IllegalArgumentException("Lecturer profile not found."));
        UserProfileForm form = normalize(rawForm);

        require(form.getHoTen(), "Full name is required.");
        require(form.getEmail(), "Email is required.");
        require(form.getThuocVien(), "Institute is required.");
        require(form.getHocVi(), "Degree is required.");
        ensureEmailAvailable(form.getEmail(), nguoiDung.getNdId());

        nguoiDung.setEmail(form.getEmail());
        nguoiDung.setCapNhat(LocalDateTime.now());
        giangVien.setHoTen(form.getHoTen());
        giangVien.setThuocVien(form.getThuocVien());
        giangVien.setHocVi(form.getHocVi());

        nguoiDungRepository.save(nguoiDung);
        giangVienRepository.save(giangVien);
    }

    @Transactional
    public void deleteAccount(String username) {
        NguoiDung nguoiDung = findNguoiDung(username);
        EntityManager activeEntityManager = Objects.requireNonNull(
            entityManager,
            "EntityManager is required for account deletion.");
        activeEntityManager.createNativeQuery("EXEC sp_DeleteNguoiDung :ndId")
            .setParameter("ndId", nguoiDung.getNdId())
            .executeUpdate();
        activeEntityManager.clear();
    }

    private NguoiDung findNguoiDung(String username) {
        return nguoiDungRepository.findByUsernameIgnoreCase(normalizeRequired(username))
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void ensureEmailAvailable(String email, String currentNdId) {
        nguoiDungRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getNdId().equals(currentNdId)) {
                throw new IllegalArgumentException("Email is already in use.");
            }
        });
    }

    private UserProfileForm normalize(UserProfileForm rawForm) {
        UserProfileForm form = new UserProfileForm();
        form.setHoTen(trim(rawForm.getHoTen()));
        form.setEmail(trimLower(rawForm.getEmail()));
        form.setTenLop(trim(rawForm.getTenLop()));
        form.setChuyenNganh(trim(rawForm.getChuyenNganh()));
        form.setNienKhoa(trim(rawForm.getNienKhoa()));
        form.setThuocVien(trim(rawForm.getThuocVien()));
        form.setHocVi(trim(rawForm.getHocVi()));
        return form;
    }

    private String normalizeRequired(String value) {
        String normalized = trim(value);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        return normalized;
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimLower(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
