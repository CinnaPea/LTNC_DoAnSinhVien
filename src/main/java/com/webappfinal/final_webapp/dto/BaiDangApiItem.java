package com.webappfinal.final_webapp.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaiDangApiItem {
    @JsonProperty("BD_ID")
    private String bdId;

    @JsonProperty("DA_ID")
    private String daId;

    @JsonProperty("TieuDe")
    private String tieuDe;

    @JsonProperty("NgayDang")
    private OffsetDateTime ngayDang;

    @JsonProperty("MoTa")
    private String moTa;

    @JsonProperty("Link")
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

    public OffsetDateTime getNgayDang() {
        return ngayDang;
    }

    public void setNgayDang(OffsetDateTime ngayDang) {
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
