package com.webappfinal.final_webapp.service;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webappfinal.final_webapp.dto.DashboardUserDTO;
import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.entity.SinhVien;
import com.webappfinal.final_webapp.repository.GiangVienRepository;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.repository.SinhVienRepository;

@Service
public class DashboardService {
    private static final String ADMIN_ROLE = "AD";

    private final GiangVienRepository giangVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SinhVienRepository sinhVienRepository;
    private final String demoUsername;
    
    public DashboardService(
            GiangVienRepository giangVienRepository,
            NguoiDungRepository nguoiDungRepository,
            SinhVienRepository sinhVienRepository,
            @Value("${app.auth.demo.username:admin1}") String demoUsername) {
        this.giangVienRepository = giangVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.demoUsername = demoUsername;
    }
    public DashboardUserDTO getDashboardUser(String username) {
        Optional<NguoiDung> nguoiDungOpt = nguoiDungRepository.findByUsernameIgnoreCase(normalize(username));
        if (nguoiDungOpt.isEmpty()) {
            if (demoUsername != null && demoUsername.equals(username)) {
                return new DashboardUserDTO(
                    "DEMO",
                    username,
                    "ADMIN",
                    "Demo User",
                    "DEMO",
                    "ADMIN",
                    "Tai khoan demo",
                    "Dang su dung dang nhap mau"
                );
            }
            throw new RuntimeException("User not found with username: " + username);
        }
        NguoiDung nguoiDung = nguoiDungOpt.get();
        if (ADMIN_ROLE.equalsIgnoreCase(nguoiDung.getVaiTroId())) {
            return new DashboardUserDTO(
                nguoiDung.getNdId(),
                username,
                nguoiDung.getVaiTroId(),
                nguoiDung.getUsername(),
                nguoiDung.getNdId(),
                "ADMIN",
                "Quan tri he thong",
                nguoiDung.getEmail()
            );
        }

        String ndId = nguoiDung.getNdId();
        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByNdId(ndId);
        if (sinhVienOpt.isPresent()) {
            SinhVien sinhVien = sinhVienOpt.get();
            return new DashboardUserDTO(
                ndId,
                username,
                "SV",
                sinhVien.getHoTen(),
                sinhVien.getMaSv(),
                "SINH_VIEN",
                sinhVien.getTenLop(),
                sinhVien.getChuyenNganh()
            );
        }

        Optional<GiangVien> giangVienOpt = giangVienRepository.findByNdId(ndId);
        if (giangVienOpt.isPresent()) {
            GiangVien giangVien = giangVienOpt.get();
            return new DashboardUserDTO(
                ndId,
                username,
                "GV",
                giangVien.getHoTen(),
                giangVien.getMaGv(),
                "GIANG_VIEN",
                giangVien.getHocVi(),
                giangVien.getThuocVien()
            );
        }
        throw new RuntimeException("Unknown user ID: " + ndId);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
