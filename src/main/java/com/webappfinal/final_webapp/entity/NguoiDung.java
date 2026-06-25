package com.webappfinal.final_webapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "NguoiDung")
public class NguoiDung {
    @Id
    @Column(name = "ND_ID", length = 10, nullable = false)
    private String ndId;

    @Column(name = "Username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "PassHash", length = 255, nullable = false)
    private String passHash;

    @Column(name = "Email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "VT_ID", length = 10, nullable = false)
    private String vaiTroId;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai;

    @Column(name = "NgayLap", nullable = false)
    private LocalDateTime ngayLap;

    @Column(name = "CapNhat")
    private LocalDateTime capNhat;

    public String getNdId() {
        return ndId;
    }

    public void setNdId(String ndId) {
        this.ndId = ndId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVaiTroId() {
        return vaiTroId;
    }

    public void setVaiTroId(String vaiTroId) {
        this.vaiTroId = vaiTroId;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public LocalDateTime getCapNhat() {
        return capNhat;
    }

    public void setCapNhat(LocalDateTime capNhat) {
        this.capNhat = capNhat;
    }
}
