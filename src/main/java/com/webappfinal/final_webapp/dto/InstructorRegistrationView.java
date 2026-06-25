package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.SinhVien;

public record InstructorRegistrationView(
        DangKyApiItem registration,
        TheLoaiApiItem topic,
        SinhVien student) {

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
        return topic != null && topic.getMaTl() != null && !topic.getMaTl().isBlank()
            ? topic.getMaTl()
            : registration.getTlId();
    }

    public String studentName() {
        return student != null && student.getHoTen() != null && !student.getHoTen().isBlank()
            ? student.getHoTen()
            : registration.getSvId();
    }
}
