package com.webappfinal.final_webapp.dto;

import com.webappfinal.final_webapp.entity.GiangVien;

public record TopicDetailView(
        TheLoaiApiItem topic,
        GiangVien lecturer,
        boolean apiAvailable,
        String message) {

    public static TopicDetailView available(TheLoaiApiItem topic, GiangVien lecturer) {
        return new TopicDetailView(topic, lecturer, true, null);
    }

    public static TopicDetailView unavailable(String message) {
        return new TopicDetailView(null, null, false, message);
    }
}
