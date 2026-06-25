package com.webappfinal.final_webapp.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DanhGiaApiItem {
    @JsonProperty("DG_ID")
    private String dgId;

    @JsonProperty("DA_ID")
    private String daId;

    @JsonProperty("GV_ID")
    private String gvId;

    @JsonProperty("DiemSo")
    private BigDecimal diemSo;

    @JsonProperty("NhanXet")
    private String nhanXet;

    @JsonProperty("NgayNX")
    private OffsetDateTime ngayNx;

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

    public OffsetDateTime getNgayNx() {
        return ngayNx;
    }

    public void setNgayNx(OffsetDateTime ngayNx) {
        this.ngayNx = ngayNx;
    }
}
