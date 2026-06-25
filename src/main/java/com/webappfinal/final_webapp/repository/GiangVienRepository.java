package com.webappfinal.final_webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webappfinal.final_webapp.entity.GiangVien;

public interface GiangVienRepository extends JpaRepository<GiangVien, String> {
    Optional<GiangVien> findByNdId(String ndId);
}

