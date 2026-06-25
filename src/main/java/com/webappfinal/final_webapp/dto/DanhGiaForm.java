package com.webappfinal.final_webapp.dto;

import java.math.BigDecimal;

public class DanhGiaForm {
    private BigDecimal diemSo;
    private String nhanXet;

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
}
