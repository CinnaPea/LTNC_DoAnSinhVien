package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.GiangVien;

public record StudentThesisView(
        DoAnApiItem thesis,
        DangKyApiItem registration,
        TheLoaiApiItem topic,
        GiangVien lecturer) {

    public boolean isCompleted() {
        return thesis != null && thesis.isCompleted();
    }

    public String topicTitle() {
        return topic != null && topic.getTenTl() != null && !topic.getTenTl().isBlank()
            ? topic.getTenTl()
            : registration != null ? registration.getTlId() : thesis.getDkId();
    }

    public String topicCode() {
        return topic != null && topic.getMaTl() != null && !topic.getMaTl().isBlank()
            ? topic.getMaTl()
            : thesis.getDaId();
    }
}
