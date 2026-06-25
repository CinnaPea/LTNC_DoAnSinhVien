package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.SinhVien;

public record AdminRegistrationView(
        DangKyApiItem registration,
        TheLoaiApiItem topic,
        SinhVien student,
        GiangVien approver) {

    public boolean isPending() {
        return registration != null && registration.isPending();
    }

    public boolean isApproved() {
        return registration != null && registration.isApproved();
    }

    public String topicTitle() {
        if (topic != null && topic.getTenTl() != null && !topic.getTenTl().isBlank()) {
            return topic.getTenTl();
        }
        return registration != null ? registration.getTlId() : "";
    }

    public String topicCode() {
        if (topic != null && topic.getMaTl() != null && !topic.getMaTl().isBlank()) {
            return topic.getMaTl();
        }
        return registration != null ? registration.getTlId() : "";
    }

    public String studentName() {
        if (student != null && student.getHoTen() != null && !student.getHoTen().isBlank()) {
            return student.getHoTen();
        }
        return registration != null ? registration.getSvId() : "";
    }

    public String approverName() {
        if (approver != null && approver.getHoTen() != null && !approver.getHoTen().isBlank()) {
            return approver.getHoTen();
        }
        return registration != null && registration.getNguoiChapThuan() != null
            ? registration.getNguoiChapThuan()
            : "Chua xu ly";
    }
}
