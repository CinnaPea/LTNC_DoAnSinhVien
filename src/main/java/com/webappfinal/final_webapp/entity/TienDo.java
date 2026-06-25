package com.webappfinal.final_webapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TienDo")
public class TienDo {
    @Id
    @Column(name = "TD_ID", nullable = false)
    private String tdId;

    @Column(name = "DA_ID", nullable = false)
    private String daId;

    @Column(name = "TieuDe", nullable = false)
    private String tieuDe;

    @Column(name = "NoiDung")
    private String noiDung;

    @Column(name = "TienDoHienTai")
    private Integer tienDoHienTai;

    @Column(name = "NgayGui", nullable = false)
    private LocalDateTime ngayGui;

    @Column(name = "NhanXet")
    private String nhanXet;

    public String getTdId() {
        return tdId;
    }

    public void setTdId(String tdId) {
        this.tdId = tdId;
    }

    public String getDaId() {
        return daId;
    }

    public void setDaId(String daId) {
        this.daId = daId;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public Integer getTienDoHienTai() {
        return tienDoHienTai;
    }

    public void setTienDoHienTai(Integer tienDoHienTai) {
        this.tienDoHienTai = tienDoHienTai;
    }

    public LocalDateTime getNgayGui() {
        return ngayGui;
    }

    public void setNgayGui(LocalDateTime ngayGui) {
        this.ngayGui = ngayGui;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }
}
