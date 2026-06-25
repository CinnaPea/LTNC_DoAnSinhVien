package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.SinhVien;

public record InstructorThesisView(
        DoAnApiItem thesis,
        DangKyApiItem registration,
        TheLoaiApiItem topic,
        SinhVien student) {

    public boolean isCompleted() {
        return thesis != null && thesis.isCompleted();
    }

    public String topicTitle() {
        return topic != null && topic.getTenTl() != null && !topic.getTenTl().isBlank()
            ? topic.getTenTl()
            : registration != null ? registration.getTlId() : thesis.getDaId();
    }

    public String topicCode() {
        return topic != null && topic.getMaTl() != null && !topic.getMaTl().isBlank()
            ? topic.getMaTl()
            : thesis.getDaId();
    }

    public String studentName() {
        return student != null && student.getHoTen() != null && !student.getHoTen().isBlank()
            ? student.getHoTen()
            : registration != null ? registration.getSvId() : thesis.getDaId();
    }
}
