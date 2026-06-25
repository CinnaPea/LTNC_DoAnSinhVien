package com.webappfinal.final_webapp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DanhGia")
public class DanhGia {
    @Id
    @Column(name = "DG_ID", nullable = false)
    private String dgId;

    @Column(name = "DA_ID", nullable = false)
    private String daId;

    @Column(name = "GV_ID", nullable = false)
    private String gvId;

    @Column(name = "DiemSo")
    private BigDecimal diemSo;

    @Column(name = "NhanXet")
    private String nhanXet;

    @Column(name = "NgayNX", nullable = false)
    private LocalDateTime ngayNx;

    public String getDgId() {
        return dgId;
    }

    public void setDgId(String dgId) {
        this.dgId = dgId;
    }

    public String getDaId() {
        return daId;
    }

    public void setDaId(String daId) {
        this.daId = daId;
    }

    public String getGvId() {
        return gvId;
    }

    public void setGvId(String gvId) {
        this.gvId = gvId;
    }

    public BigDecimal getDiemSo() {
        return diemSo;
    }

    public void setDiemSo(BigDecimal diemSo) {
        this.diemSo = diemSo;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }

    public LocalDateTime getNgayNx() {
        return ngayNx;
    }

    public void setNgayNx(LocalDateTime ngayNx) {
        this.ngayNx = ngayNx;
    }
}
