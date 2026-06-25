package com.webappfinal.final_webapp.dto;

import java.text.Normalizer;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DoAnApiItem {
    @JsonProperty("DA_ID")
    private String daId;

    @JsonProperty("DK_ID")
    private String dkId;

    @JsonProperty("GV_ID")
    private String gvId;

    @JsonProperty("TrangThai")
    private String trangThai;

    @JsonProperty("NgayThucHien")
    private OffsetDateTime ngayThucHien;

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

    public OffsetDateTime getNgayThucHien() {
        return ngayThucHien;
    }

    public void setNgayThucHien(OffsetDateTime ngayThucHien) {
        this.ngayThucHien = ngayThucHien;
    }

    public boolean isCompleted() {
        return "hoan thanh".equals(normalize(trangThai));
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
