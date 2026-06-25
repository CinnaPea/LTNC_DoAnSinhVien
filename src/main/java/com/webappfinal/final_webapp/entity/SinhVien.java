package com.webappfinal.final_webapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SinhVien")
public class SinhVien {
    @Id
    @Column(name = "SV_ID", nullable = false)
    private String svId;

    @Column(name = "ND_ID", nullable = false)
    private String ndId;

    @Column(name = "MaSV")
    private String maSv;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "TenLop")
    private String tenLop;

    @Column(name = "ChuyenNganh")
    private String chuyenNganh;

    @Column(name = "NienKhoa")
    private String nienKhoa;

    public String getSvId() {
        return svId;
    }

    public void setSvId(String svId) {
        this.svId = svId;
    }

    public String getNdId() {
        return ndId;
    }

    public void setNdId(String ndId) {
        this.ndId = ndId;
    }

    public String getMaSv() {
        return maSv;
    }

    public void setMaSv(String maSv) {
        this.maSv = maSv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getChuyenNganh() {
        return chuyenNganh;
    }

    public void setChuyenNganh(String chuyenNganh) {
        this.chuyenNganh = chuyenNganh;
    }

    public String getNienKhoa() {
        return nienKhoa;
    }

    public void setNienKhoa(String nienKhoa) {
        this.nienKhoa = nienKhoa;
    }
}
