package com.webappfinal.final_webapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DangKy")
public class DangKy {
    @Id
    @Column(name = "DK_ID", nullable = false)
    private String dkId;

    @Column(name = "NgayDangKy", nullable = false)
    private LocalDateTime ngayDangKy;

    @Column(name = "SV_ID", nullable = false)
    private String svId;

    @Column(name = "TL_ID", nullable = false)
    private String tlId;

    @Column(name = "NguoiChapThuan")
    private String nguoiChapThuan;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai;

    @Column(name = "NgayChapThuan")
    private LocalDateTime ngayChapThuan;

    @Column(name = "GhiChu")
    private String ghiChu;

    public String getDkId() {
        return dkId;
    }

    public void setDkId(String dkId) {
        this.dkId = dkId;
    }

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public String getSvId() {
        return svId;
    }

    public void setSvId(String svId) {
        this.svId = svId;
    }

    public String getTlId() {
        return tlId;
    }

    public void setTlId(String tlId) {
        this.tlId = tlId;
    }

    public String getNguoiChapThuan() {
        return nguoiChapThuan;
    }

    public void setNguoiChapThuan(String nguoiChapThuan) {
        this.nguoiChapThuan = nguoiChapThuan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayChapThuan() {
        return ngayChapThuan;
    }

    public void setNgayChapThuan(LocalDateTime ngayChapThuan) {
        this.ngayChapThuan = ngayChapThuan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
