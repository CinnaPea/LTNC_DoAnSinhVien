package com.webappfinal.final_webapp.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;

@Service
public class DatabaseAuthenticationService {
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseAuthenticationService(NguoiDungRepository nguoiDungRepository, PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isValidCredentials(String username, String password) {
        Optional<NguoiDung> user = nguoiDungRepository.findByUsernameIgnoreCase(normalize(username));
        if (user.isEmpty()) {
            return false;
        }

        NguoiDung nguoiDung = user.get();
        if (!isActive(nguoiDung.getTrangThai())) {
            return false;
        }

        return passwordMatches(password, nguoiDung.getPassHash());
    }

    private boolean isActive(Boolean trangThai) {
        return trangThai == null || trangThai;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }

        String stored = storedPassword.trim();
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, stored);
        }

        return rawPassword.equals(stored);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
