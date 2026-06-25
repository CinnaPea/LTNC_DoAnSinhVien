package com.webappfinal.final_webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webappfinal.final_webapp.entity.NguoiDung;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {
    Optional<NguoiDung> findByUsername(String username);
    Optional<NguoiDung> findByUsernameIgnoreCase(String username);
    Optional<NguoiDung> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmail(String email);
}
