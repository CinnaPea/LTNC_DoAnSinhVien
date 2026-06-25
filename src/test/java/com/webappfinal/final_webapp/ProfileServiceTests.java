package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webappfinal.final_webapp.dto.UserProfileForm;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;
import com.webappfinal.final_webapp.service.ProfileService;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTests {
    @Mock
    private GiangVienRepository giangVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Test
    void loadsStudentProfileFromNguoiDungAndSinhVien() {
        ProfileService service = createService();
        NguoiDung nguoiDung = nguoiDung("ND0001", "student01", "student01@email.com");
        SinhVien sinhVien = student("ND0001", "SV0001");
        sinhVien.setHoTen("Student One");
        sinhVien.setTenLop("CTK45");
        sinhVien.setChuyenNganh("CNTT");
        sinhVien.setNienKhoa("2022-2027");

        when(nguoiDungRepository.findByUsernameIgnoreCase("student01")).thenReturn(Optional.of(nguoiDung));
        when(sinhVienRepository.findByNdId("ND0001")).thenReturn(Optional.of(sinhVien));

        UserProfileForm form = service.getStudentProfile("student01");

        assertThat(form.getUsername()).isEqualTo("student01");
        assertThat(form.getProfileCode()).isEqualTo("SV0001");
        assertThat(form.getEmail()).isEqualTo("student01@email.com");
        assertThat(form.getHoTen()).isEqualTo("Student One");
    }

    @Test
    void updatesStudentProfileAndNguoiDungEmail() {
        ProfileService service = createService();
        NguoiDung nguoiDung = nguoiDung("ND0001", "student01", "old@email.com");
        SinhVien sinhVien = student("ND0001", "SV0001");
        UserProfileForm form = new UserProfileForm();
        form.setHoTen("Student Updated");
        form.setEmail("new@email.com");
        form.setTenLop("CTK46");
        form.setChuyenNganh("AI");
        form.setNienKhoa("2023-2028");

        when(nguoiDungRepository.findByUsernameIgnoreCase("student01")).thenReturn(Optional.of(nguoiDung));
        when(sinhVienRepository.findByNdId("ND0001")).thenReturn(Optional.of(sinhVien));
        when(nguoiDungRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());

        service.updateStudentProfile("student01", form);

        assertThat(nguoiDung.getEmail()).isEqualTo("new@email.com");
        assertThat(nguoiDung.getCapNhat()).isNotNull();
        assertThat(sinhVien.getHoTen()).isEqualTo("Student Updated");
        assertThat(sinhVien.getTenLop()).isEqualTo("CTK46");
        verify(nguoiDungRepository).save(nguoiDung);
        verify(sinhVienRepository).save(sinhVien);
    }

    @Test
    void blocksStudentUpdateWhenEmailBelongsToAnotherAccount() {
        ProfileService service = createService();
        NguoiDung current = nguoiDung("ND0001", "student01", "old@email.com");
        NguoiDung other = nguoiDung("ND0009", "other", "used@email.com");
        SinhVien sinhVien = student("ND0001", "SV0001");
        UserProfileForm form = new UserProfileForm();
        form.setHoTen("Student Updated");
        form.setEmail("used@email.com");
        form.setTenLop("CTK46");
        form.setChuyenNganh("AI");
        form.setNienKhoa("2023-2028");

        when(nguoiDungRepository.findByUsernameIgnoreCase("student01")).thenReturn(Optional.of(current));
        when(sinhVienRepository.findByNdId("ND0001")).thenReturn(Optional.of(sinhVien));
        when(nguoiDungRepository.findByEmail("used@email.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.updateStudentProfile("student01", form))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is already in use.");
    }

    @Test
    void updatesInstructorProfile() {
        ProfileService service = createService();
        NguoiDung nguoiDung = nguoiDung("ND0002", "lecturer01", "old@lecturer.com");
        GiangVien giangVien = lecturer("ND0002", "GV0001");
        UserProfileForm form = new UserProfileForm();
        form.setHoTen("Lecturer Updated");
        form.setEmail("new@lecturer.com");
        form.setHocVi("Doctor");
        form.setThuocVien("Microservices Lab");

        when(nguoiDungRepository.findByUsernameIgnoreCase("lecturer01")).thenReturn(Optional.of(nguoiDung));
        when(giangVienRepository.findByNdId("ND0002")).thenReturn(Optional.of(giangVien));
        when(nguoiDungRepository.findByEmail("new@lecturer.com")).thenReturn(Optional.empty());

        service.updateInstructorProfile("lecturer01", form);

        assertThat(nguoiDung.getEmail()).isEqualTo("new@lecturer.com");
        assertThat(giangVien.getHoTen()).isEqualTo("Lecturer Updated");
        assertThat(giangVien.getHocVi()).isEqualTo("Doctor");
        assertThat(giangVien.getThuocVien()).isEqualTo("Microservices Lab");
        verify(nguoiDungRepository).save(nguoiDung);
        verify(giangVienRepository).save(giangVien);
    }

    private ProfileService createService() {
        return new ProfileService(giangVienRepository, nguoiDungRepository, sinhVienRepository);
    }

    private NguoiDung nguoiDung(String ndId, String username, String email) {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setNdId(ndId);
        nguoiDung.setUsername(username);
        nguoiDung.setEmail(email);
        return nguoiDung;
    }

    private SinhVien student(String ndId, String maSv) {
        SinhVien sinhVien = new SinhVien();
        sinhVien.setNdId(ndId);
        sinhVien.setMaSv(maSv);
        return sinhVien;
    }

    private GiangVien lecturer(String ndId, String maGv) {
        GiangVien giangVien = new GiangVien();
        giangVien.setNdId(ndId);
        giangVien.setMaGv(maGv);
        return giangVien;
    }
}
