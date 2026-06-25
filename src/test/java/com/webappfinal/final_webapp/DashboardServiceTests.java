package com.webappfinal.final_webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webappfinal.final_webapp.dto.DashboardUserDTO;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;
import com.webappfinal.final_webapp.service.DashboardService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {
    @Mock
    private GiangVienRepository giangVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
            giangVienRepository,
            nguoiDungRepository,
            sinhVienRepository,
            "admin1"
        );
    }

    @Test
    void returnsDemoDashboardUserWhenDemoAccountIsNotInDatabase() {
        when(nguoiDungRepository.findByUsernameIgnoreCase("admin1")).thenReturn(Optional.empty());

        DashboardUserDTO user = dashboardService.getDashboardUser("admin1");

        assertEquals("admin1", user.getUsername());
        assertEquals("ADMIN", user.getLoaiTK());
        verify(sinhVienRepository, never()).findByNdId("DEMO");
    }

    @Test
    void returnsAdminDashboardUserWhenNguoiDungHasAdminRole() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setNdId("ND0006");
        nguoiDung.setUsername("Admin");
        nguoiDung.setVaiTroId("AD");
        nguoiDung.setEmail("admin@admin.com");

        when(nguoiDungRepository.findByUsernameIgnoreCase("Admin")).thenReturn(Optional.of(nguoiDung));

        DashboardUserDTO user = dashboardService.getDashboardUser("Admin");

        assertEquals("ADMIN", user.getLoaiTK());
        assertEquals("ND0006", user.getMaTK());
        verify(sinhVienRepository, never()).findByNdId("ND0006");
        verify(giangVienRepository, never()).findByNdId("ND0006");
    }

    @Test
    void returnsStudentDashboardUserWhenNdIdMatchesSinhVien() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setNdId("ND0001");
        nguoiDung.setUsername("student01");

        SinhVien sinhVien = new SinhVien();
        sinhVien.setNdId("ND0001");
        sinhVien.setHoTen("Student One");
        sinhVien.setMaSv("SV0001");
        sinhVien.setTenLop("CTK42");
        sinhVien.setChuyenNganh("CNTT");

        when(nguoiDungRepository.findByUsernameIgnoreCase("student01")).thenReturn(Optional.of(nguoiDung));
        when(sinhVienRepository.findByNdId("ND0001")).thenReturn(Optional.of(sinhVien));

        DashboardUserDTO user = dashboardService.getDashboardUser("student01");

        assertEquals("SINH_VIEN", user.getLoaiTK());
        assertEquals("Student One", user.getHoTen());
        assertEquals("SV0001", user.getMaTK());
        verify(sinhVienRepository).findByNdId("ND0001");
    }

    @Test
    void returnsLecturerDashboardUserWhenNdIdMatchesGiangVien() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setNdId("ND0002");
        nguoiDung.setUsername("lecturer01");

        GiangVien giangVien = new GiangVien();
        giangVien.setNdId("ND0002");
        giangVien.setHoTen("Lecturer One");
        giangVien.setMaGv("GV0001");
        giangVien.setHocVi("ThS");
        giangVien.setThuocVien("CNTT");

        when(nguoiDungRepository.findByUsernameIgnoreCase("lecturer01")).thenReturn(Optional.of(nguoiDung));
        when(sinhVienRepository.findByNdId("ND0002")).thenReturn(Optional.empty());
        when(giangVienRepository.findByNdId("ND0002")).thenReturn(Optional.of(giangVien));

        DashboardUserDTO user = dashboardService.getDashboardUser("lecturer01");

        assertEquals("GIANG_VIEN", user.getLoaiTK());
        assertEquals("Lecturer One", user.getHoTen());
        assertEquals("GV0001", user.getMaTK());
        verify(giangVienRepository).findByNdId("ND0002");
    }

    @Test
    void throwsWhenNguoiDungHasNoLinkedStudentOrLecturerRecord() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setNdId("ND9999");
        nguoiDung.setUsername("mystery");

        when(nguoiDungRepository.findByUsernameIgnoreCase("mystery")).thenReturn(Optional.of(nguoiDung));
        when(sinhVienRepository.findByNdId("ND9999")).thenReturn(Optional.empty());
        when(giangVienRepository.findByNdId("ND9999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> dashboardService.getDashboardUser("mystery"));

        assertEquals("Unknown user ID: ND9999", exception.getMessage());
    }
}
