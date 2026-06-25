package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.GiangVien;
import com.webappfinal.final_webapp.entity.SinhVien;

public record AdminThesisView(
        DoAnApiItem thesis,
        DangKyApiItem registration,
        TheLoaiApiItem topic,
        SinhVien student,
        GiangVien lecturer) {

    public boolean isCompleted() {
        return thesis != null && thesis.isCompleted();
    }

    public String topicTitle() {
        if (topic != null && topic.getTenTl() != null && !topic.getTenTl().isBlank()) {
            return topic.getTenTl();
        }
        if (registration != null && registration.getTlId() != null) {
            return registration.getTlId();
        }
        return thesis != null ? thesis.getDaId() : "";
    }

    public String topicCode() {
        if (topic != null && topic.getMaTl() != null && !topic.getMaTl().isBlank()) {
            return topic.getMaTl();
        }
        return thesis != null ? thesis.getDaId() : "";
    }

    public String studentName() {
        if (student != null && student.getHoTen() != null && !student.getHoTen().isBlank()) {
            return student.getHoTen();
        }
        return registration != null ? registration.getSvId() : "";
    }

    public String lecturerName() {
        if (lecturer != null && lecturer.getHoTen() != null && !lecturer.getHoTen().isBlank()) {
            return lecturer.getHoTen();
        }
        return thesis != null ? thesis.getGvId() : "";
    }
}
