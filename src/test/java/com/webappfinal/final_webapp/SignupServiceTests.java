package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.webappfinal.final_webapp.dto.SignupForm;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;
import com.webappfinal.final_webapp.repository.VaiTroRepository;
import com.webappfinal.final_webapp.service.IdGenerationService;
import com.webappfinal.final_webapp.service.SignupService;

@ExtendWith(MockitoExtension.class)
class SignupServiceTests {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private GiangVienRepository giangVienRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @Mock
    private IdGenerationService idGenerationService;

    @Test
    void registersStudentAccountAndUpdatesTriggerCreatedProfile() {
        SignupService signupService = createService();
        SignupForm form = studentForm();
        SinhVien triggeredSinhVien = new SinhVien();
        triggeredSinhVien.setSvId("SV0002");
        triggeredSinhVien.setNdId("ND0003");
        triggeredSinhVien.setMaSv("SV0002");

        when(vaiTroRepository.existsById("SV")).thenReturn(true);
        when(idGenerationService.allocateNextId(IdGenerationService.NGUOI_DUNG)).thenReturn("ND0003");
        when(nguoiDungRepository.existsById("ND0003")).thenReturn(false);
        when(giangVienRepository.findByNdId("ND0003")).thenReturn(Optional.empty());
        when(sinhVienRepository.findByNdId("ND0003")).thenReturn(Optional.empty(), Optional.of(triggeredSinhVien));

        signupService.register(form);

        ArgumentCaptor<NguoiDung> nguoiDungCaptor = ArgumentCaptor.forClass(NguoiDung.class);
        ArgumentCaptor<SinhVien> sinhVienCaptor = ArgumentCaptor.forClass(SinhVien.class);

        verify(nguoiDungRepository).saveAndFlush(nguoiDungCaptor.capture());
        verify(sinhVienRepository).save(sinhVienCaptor.capture());
        verify(giangVienRepository, never()).save(org.mockito.ArgumentMatchers.any(GiangVien.class));

        NguoiDung nguoiDung = nguoiDungCaptor.getValue();
        SinhVien sinhVien = sinhVienCaptor.getValue();

        assertThat(nguoiDung.getNdId()).isEqualTo("ND0003");
        assertThat(nguoiDung.getUsername()).isEqualTo("student_new");
        assertThat(nguoiDung.getVaiTroId()).isEqualTo("SV");
        assertThat(nguoiDung.getTrangThai()).isTrue();
        assertThat(passwordEncoder.matches("secret123", nguoiDung.getPassHash())).isTrue();

        assertThat(sinhVien.getSvId()).isEqualTo("SV0002");
        assertThat(sinhVien.getNdId()).isEqualTo("ND0003");
        assertThat(sinhVien.getMaSv()).isEqualTo("SV0002");
        assertThat(sinhVien.getHoTen()).isEqualTo("Student New");
        assertThat(sinhVien.getTenLop()).isEqualTo("CTK45");
    }

    @Test
    void skipsAlreadyUsedNguoiDungIdAndRetries() {
        SignupService signupService = createService();
        SignupForm form = lecturerForm();

        GiangVien existingLink = new GiangVien();
        existingLink.setNdId("ND0003");

        GiangVien triggeredGiangVien = new GiangVien();
        triggeredGiangVien.setGvId("GV0002");
        triggeredGiangVien.setNdId("ND0004");
        triggeredGiangVien.setMaGv("GV0002");

        when(vaiTroRepository.existsById("GV")).thenReturn(true);
        when(idGenerationService.allocateNextId(IdGenerationService.NGUOI_DUNG)).thenReturn("ND0003", "ND0004");
        when(nguoiDungRepository.existsById("ND0003")).thenReturn(false);
        when(nguoiDungRepository.existsById("ND0004")).thenReturn(false);
        when(sinhVienRepository.findByNdId("ND0003")).thenReturn(Optional.empty());
        when(sinhVienRepository.findByNdId("ND0004")).thenReturn(Optional.empty());
        when(giangVienRepository.findByNdId("ND0003")).thenReturn(Optional.of(existingLink));
        when(giangVienRepository.findByNdId("ND0004")).thenReturn(Optional.empty(), Optional.of(triggeredGiangVien));

        signupService.register(form);

        ArgumentCaptor<NguoiDung> nguoiDungCaptor = ArgumentCaptor.forClass(NguoiDung.class);
        verify(nguoiDungRepository).saveAndFlush(nguoiDungCaptor.capture());
        verify(idGenerationService, times(2)).allocateNextId(IdGenerationService.NGUOI_DUNG);
        assertThat(nguoiDungCaptor.getValue().getNdId()).isEqualTo("ND0004");
    }

    @Test
    void registersLecturerAccountAndUpdatesTriggerCreatedProfile() {
        SignupService signupService = createService();
        SignupForm form = lecturerForm();
        GiangVien triggeredGiangVien = new GiangVien();
        triggeredGiangVien.setGvId("GV0002");
        triggeredGiangVien.setNdId("ND0004");
        triggeredGiangVien.setMaGv("GV0002");

        when(vaiTroRepository.existsById("GV")).thenReturn(true);
        when(idGenerationService.allocateNextId(IdGenerationService.NGUOI_DUNG)).thenReturn("ND0004");
        when(nguoiDungRepository.existsById("ND0004")).thenReturn(false);
        when(sinhVienRepository.findByNdId("ND0004")).thenReturn(Optional.empty());
        when(giangVienRepository.findByNdId("ND0004")).thenReturn(Optional.empty(), Optional.of(triggeredGiangVien));

        signupService.register(form);

        ArgumentCaptor<GiangVien> giangVienCaptor = ArgumentCaptor.forClass(GiangVien.class);
        verify(giangVienRepository).save(giangVienCaptor.capture());

        GiangVien giangVien = giangVienCaptor.getValue();
        assertThat(giangVien.getGvId()).isEqualTo("GV0002");
        assertThat(giangVien.getNdId()).isEqualTo("ND0004");
        assertThat(giangVien.getMaGv()).isEqualTo("GV0002");
        assertThat(giangVien.getHoTen()).isEqualTo("Lecturer New");
        assertThat(giangVien.getThuocVien()).isEqualTo("CNTT");
        assertThat(giangVien.getHocVi()).isEqualTo("ThS");
    }

    @Test
    void failsClearlyWhenStudentTriggerRowIsMissing() {
        SignupService signupService = createService();
        SignupForm form = studentForm();

        when(vaiTroRepository.existsById("SV")).thenReturn(true);
        when(idGenerationService.allocateNextId(IdGenerationService.NGUOI_DUNG)).thenReturn("ND0003");
        when(nguoiDungRepository.existsById("ND0003")).thenReturn(false);
        when(giangVienRepository.findByNdId("ND0003")).thenReturn(Optional.empty());
        when(sinhVienRepository.findByNdId("ND0003")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> signupService.register(form))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Student profile was not created by the database trigger.");
    }

    @Test
    void rejectsMismatchedPasswordConfirmation() {
        SignupService signupService = createService();
        SignupForm form = studentForm();
        form.setConfirmPassword("different");

        assertThatThrownBy(() -> signupService.register(form))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password confirmation does not match.");
    }

    @Test
    void rejectsDuplicateUsername() {
        SignupService signupService = createService();
        SignupForm form = studentForm();

        when(vaiTroRepository.existsById("SV")).thenReturn(true);
        when(nguoiDungRepository.existsByUsernameIgnoreCase("student_new")).thenReturn(true);

        assertThatThrownBy(() -> signupService.register(form))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username is already taken.");
    }

    private SignupService createService() {
        return new SignupService(
            nguoiDungRepository,
            sinhVienRepository,
            giangVienRepository,
            vaiTroRepository,
            idGenerationService,
            passwordEncoder
        );
    }

    private SignupForm studentForm() {
        SignupForm form = new SignupForm();
        form.setAccountType("SV");
        form.setUsername("student_new");
        form.setPassword("secret123");
        form.setConfirmPassword("secret123");
        form.setEmail("student_new@email.com");
        form.setHoTen("Student New");
        form.setTenLop("CTK45");
        form.setChuyenNganh("CNTT");
        form.setNienKhoa("2026");
        return form;
    }

    private SignupForm lecturerForm() {
        SignupForm form = new SignupForm();
        form.setAccountType("GV");
        form.setUsername("lecturer_new");
        form.setPassword("secret123");
        form.setConfirmPassword("secret123");
        form.setEmail("lecturer_new@email.com");
        form.setHoTen("Lecturer New");
        form.setThuocVien("CNTT");
        form.setHocVi("ThS");
        return form;
    }
}
