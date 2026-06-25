package com.webappfinal.final_webapp.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminUserApiItem {
    @JsonProperty("ND_ID")
    private String ndId;

    @JsonProperty("Username")
    private String username;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("VT_ID")
    private String vaiTroId;

    @JsonProperty("RoleName")
    private String roleName;

    @JsonProperty("TrangThai")
    private Boolean trangThai;

    @JsonProperty("NgayLap")
    private OffsetDateTime ngayLap;

    @JsonProperty("CapNhat")
    private OffsetDateTime capNhat;

    @JsonProperty("ProfileType")
    private String profileType;

    @JsonProperty("ProfileId")
    private String profileId;

    @JsonProperty("ProfileName")
    private String profileName;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVaiTroId() {
        return vaiTroId;
    }

    public void setVaiTroId(String vaiTroId) {
        this.vaiTroId = vaiTroId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public OffsetDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(OffsetDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public OffsetDateTime getCapNhat() {
        return capNhat;
    }

    public void setCapNhat(OffsetDateTime capNhat) {
        this.capNhat = capNhat;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public boolean isActive() {
        return trangThai == null || trangThai;
    }

    public String getDisplayRoleName() {
        return roleName == null || roleName.isBlank() ? vaiTroId : roleName;
    }

    public String getDisplayProfileType() {
        return profileType == null || profileType.isBlank() ? "Khong co" : profileType;
    }

    public String getDisplayProfileId() {
        return profileId == null || profileId.isBlank() ? "Khong co" : profileId;
    }

    public String getDisplayProfileName() {
        return profileName == null || profileName.isBlank() ? "Chua dong bo" : profileName;
    }

    public String getStatusLabel() {
        return isActive() ? "Dang hoat dong" : "Da khoa";
    }
}
