package com.webappfinal.final_webapp.dto;

import java.text.Normalizer;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TheLoaiApiItem {
    @JsonProperty("TL_ID")
    private String tlId;

    @JsonProperty("MaTL")
    private String maTl;

    @JsonProperty("TenTL")
    private String tenTl;

    @JsonProperty("MoTa")
    private String moTa;

    @JsonProperty("GV_ID")
    private String gvId;

    @JsonProperty("TrangThai")
    private String trangThai;

    @JsonProperty("NgayLap")
    private OffsetDateTime ngayLap;

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

    public OffsetDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(OffsetDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public boolean isOpen() {
        return "mo".equals(normalize(trangThai));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('\u0110', 'D')
            .replace('\u0111', 'd');

        return normalized.trim().toLowerCase();
    }
}
