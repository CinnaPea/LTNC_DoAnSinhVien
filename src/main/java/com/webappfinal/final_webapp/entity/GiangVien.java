package com.webappfinal.final_webapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GiangVien")
public class GiangVien {
    @Id
    @Column(name = "GV_ID", nullable = false)
    private String gvId;

    @Column(name = "ND_ID", nullable = false)
    private String ndId;

    @Column(name = "MaGV")
    private String maGv;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "ThuocVien")
    private String thuocVien;

    @Column(name = "HocVi")
    private String hocVi;

    public String getGvId() {
        return gvId;
    }

    public void setGvId(String gvId) {
        this.gvId = gvId;
    }

    public String getNdId() {
        return ndId;
    }

    public void setNdId(String ndId) {
        this.ndId = ndId;
    }

    public String getMaGv() {
        return maGv;
    }

    public void setMaGv(String maGv) {
        this.maGv = maGv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getThuocVien() {
        return thuocVien;
    }

    public void setThuocVien(String thuocVien) {
        this.thuocVien = thuocVien;
    }

    public String getHocVi() {
        return hocVi;
    }

    public void setHocVi(String hocVi) {
        this.hocVi = hocVi;
    }
}
