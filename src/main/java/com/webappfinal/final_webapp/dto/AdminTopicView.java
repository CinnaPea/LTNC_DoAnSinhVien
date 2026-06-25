package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.GiangVien;

public record AdminTopicView(
        TheLoaiApiItem topic,
        GiangVien lecturer) {

    public boolean isOpen() {
        return topic != null && topic.isOpen();
    }

    public String topicCode() {
        if (topic == null) {
            return "";
        }
        return topic.getMaTl() != null && !topic.getMaTl().isBlank() ? topic.getMaTl() : topic.getTlId();
    }

    public String topicTitle() {
        if (topic == null) {
            return "";
        }
        return topic.getTenTl() != null && !topic.getTenTl().isBlank() ? topic.getTenTl() : topic.getTlId();
    }

    public String lecturerName() {
        if (lecturer != null && lecturer.getHoTen() != null && !lecturer.getHoTen().isBlank()) {
            return lecturer.getHoTen();
        }
        return topic != null ? topic.getGvId() : "";
    }

    public String lecturerCode() {
        return lecturer != null && lecturer.getMaGv() != null && !lecturer.getMaGv().isBlank()
            ? lecturer.getMaGv()
            : topic != null ? topic.getGvId() : "";
    }
}
