package com.webappfinal.final_webapp.dto;

public class DashboardUserDTO {
    private String ndId;
    private String username;
    private String vaiTroId;
    private String hoTen;
    private String maTK;
    private String loaiTK;
    private String ghiChu1;
    private String ghiChu2;

    public DashboardUserDTO() {
    }
    public DashboardUserDTO(String ndId, String username, String vaiTroId, String hoTen, String maTK, String loaiTK, String ghiChu1, String ghiChu2) {
        this.ndId = ndId;
        this.username = username;
        this.vaiTroId = vaiTroId;
        this.hoTen = hoTen;
        this.maTK = maTK;
        this.loaiTK = loaiTK;
        this.ghiChu1 = ghiChu1;
        this.ghiChu2 = ghiChu2;
    }
    public String getNdId() {
        return ndId;
    }
    public void setNdId(String ndId) {
        this.ndId = ndId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getVaiTroId() {
        return vaiTroId;
    }
    public void setVaiTroId(String vaiTroId) {
        this.vaiTroId = vaiTroId;
    }
    public String getHoTen() {
        return hoTen;
    }
    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }
    public String getMaTK() {
        return maTK;
    }
    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }
    public String getLoaiTK() {
        return loaiTK;
    }
    public void setLoaiTK(String loaiTK) {
        this.loaiTK = loaiTK;
    }
    public String getGhiChu1() {
        return ghiChu1;
    }
    public void setGhiChu1(String ghiChu1) {
        this.ghiChu1 = ghiChu1;
    }
    public String getGhiChu2() {
        return ghiChu2;
    }
    public void setGhiChu2(String ghiChu2) {
        this.ghiChu2 = ghiChu2;
    }

    public String getDisplayName() {
        return hoTen;
    }

    public String getProfileCode() {
        return maTK;
    }

    public String getExtraInfo1() {
        return ghiChu1;
    }

    public String getExtraInfo2() {
        return ghiChu2;
    }
}
