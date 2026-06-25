package com.webappfinal.final_webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webappfinal.final_webapp.entity.SinhVien;

public interface SinhVienRepository extends JpaRepository<SinhVien, String> {
    Optional<SinhVien> findByNdId(String ndId);
}
