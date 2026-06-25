package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.GiangVien;

public record StudentRegistrationView(
        DangKyApiItem registration,
        TheLoaiApiItem topic,
        GiangVien lecturer) {

    public boolean isPending() {
        return registration != null && registration.isPending();
    }

    public boolean isApproved() {
        return registration != null && registration.isApproved();
    }

    public boolean isRejected() {
        return registration != null && registration.isRejected();
    }

    public String topicTitle() {
        return topic != null && topic.getTenTl() != null && !topic.getTenTl().isBlank()
            ? topic.getTenTl()
            : registration.getTlId();
    }

    public String topicCode() {
        return topic != null ? topic.getMaTl() : registration.getTlId();
    }
}
