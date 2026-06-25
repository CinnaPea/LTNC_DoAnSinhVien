package com.webappfinal.final_webapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DoAn")
public class DoAn {
    @Id
    @Column(name = "DA_ID", nullable = false)
    private String daId;

    @Column(name = "DK_ID", nullable = false)
    private String dkId;

    @Column(name = "GV_ID", nullable = false)
    private String gvId;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai;

    @Column(name = "NgayThucHien", nullable = false)
    private LocalDateTime ngayThucHien;

    public String getDaId() {
        return daId;
    }

    public void setDaId(String daId) {
        this.daId = daId;
    }

    public String getDkId() {
        return dkId;
    }

    public void setDkId(String dkId) {
        this.dkId = dkId;
    }

    public String getGvId() {
        return gvId;
    }

    public void setGvId(String gvId) {
        this.gvId = gvId;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayThucHien() {
        return ngayThucHien;
    }

    public void setNgayThucHien(LocalDateTime ngayThucHien) {
        this.ngayThucHien = ngayThucHien;
    }
}
