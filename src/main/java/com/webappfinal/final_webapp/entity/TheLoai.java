package com.webappfinal.final_webapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TheLoai")
public class TheLoai {
    @Id
    @Column(name = "TL_ID", nullable = false)
    private String tlId;

    @Column(name = "MaTL")
    private String maTl;

    @Column(name = "TenTL", nullable = false)
    private String tenTl;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "GV_ID", nullable = false)
    private String gvId;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai;

    @Column(name = "NgayLap", nullable = false)
    private LocalDateTime ngayLap;

    public String getTlId() {
        return tlId;
    }

    public void setTlId(String tlId) {
        this.tlId = tlId;
    }

    public String getMaTl() {
        return maTl;
    }

    public void setMaTl(String maTl) {
        this.maTl = maTl;
    }

    public String getTenTl() {
        return tenTl;
    }

    public void setTenTl(String tenTl) {
        this.tenTl = tenTl;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
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

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }
}
