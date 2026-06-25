package com.webappfinal.final_webapp.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TienDoApiItem {
    @JsonProperty("TD_ID")
    private String tdId;

    @JsonProperty("DA_ID")
    private String daId;

    @JsonProperty("TieuDe")
    private String tieuDe;

    @JsonProperty("NoiDung")
    private String noiDung;

    @JsonProperty("TienDoHienTai")
    private Integer tienDoHienTai;

    @JsonProperty("NgayGui")
    private OffsetDateTime ngayGui;

    @JsonProperty("NhanXet")
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

    public OffsetDateTime getNgayGui() {
        return ngayGui;
    }

    public void setNgayGui(OffsetDateTime ngayGui) {
        this.ngayGui = ngayGui;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }
}
