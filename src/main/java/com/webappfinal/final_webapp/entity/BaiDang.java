package com.webappfinal.final_webapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BaiDang")
public class BaiDang {
    @Id
    @Column(name = "BD_ID", nullable = false)
    private String bdId;

    @Column(name = "DA_ID", nullable = false)
    private String daId;

    @Column(name = "TieuDe", nullable = false)
    private String tieuDe;

    @Column(name = "NgayDang", nullable = false)
    private LocalDateTime ngayDang;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "Link")
    private String link;

    public String getBdId() {
        return bdId;
    }

    public void setBdId(String bdId) {
        this.bdId = bdId;
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

    public LocalDateTime getNgayDang() {
        return ngayDang;
    }

    public void setNgayDang(LocalDateTime ngayDang) {
        this.ngayDang = ngayDang;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
