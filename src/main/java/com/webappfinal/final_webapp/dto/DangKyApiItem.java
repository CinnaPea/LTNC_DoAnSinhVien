package com.webappfinal.final_webapp.dto;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DangKyApiItem {
    private static final Set<String> PENDING_STATUSES = Set.of("cho duyet", "dang kiem duyet");

    @JsonProperty("DK_ID")
    private String dkId;

    @JsonProperty("NgayDangKy")
    private OffsetDateTime ngayDangKy;

    @JsonProperty("SV_ID")
    private String svId;

    @JsonProperty("TL_ID")
    private String tlId;

    @JsonProperty("NguoiChapThuan")
    private String nguoiChapThuan;

    @JsonProperty("TrangThai")
    private String trangThai;

    @JsonProperty("NgayChapThuan")
    private OffsetDateTime ngayChapThuan;

    @JsonProperty("GhiChu")
    private String ghiChu;

    public String getDkId() {
        return dkId;
    }

    public void setDkId(String dkId) {
        this.dkId = dkId;
    }

    public OffsetDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(OffsetDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public String getSvId() {
        return svId;
    }

    public void setSvId(String svId) {
        this.svId = svId;
    }

    public String getTlId() {
        return tlId;
    }

    public void setTlId(String tlId) {
        this.tlId = tlId;
    }

    public String getNguoiChapThuan() {
        return nguoiChapThuan;
    }

    public void setNguoiChapThuan(String nguoiChapThuan) {
        this.nguoiChapThuan = nguoiChapThuan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public OffsetDateTime getNgayChapThuan() {
        return ngayChapThuan;
    }

    public void setNgayChapThuan(OffsetDateTime ngayChapThuan) {
        this.ngayChapThuan = ngayChapThuan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public boolean isPending() {
        return PENDING_STATUSES.contains(normalize(trangThai));
    }

    public boolean isApproved() {
        return "da duyet".equals(normalize(trangThai));
    }

    public boolean isRejected() {
        return "tu choi".equals(normalize(trangThai));
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
