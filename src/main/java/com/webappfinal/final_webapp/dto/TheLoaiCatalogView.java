package com.webappfinal.final_webapp.dto;

import java.util.List;

public record TheLoaiCatalogView(
        List<TheLoaiApiItem> topics,
        boolean apiAvailable,
        String message) {

    public static TheLoaiCatalogView available(List<TheLoaiApiItem> topics) {
        return new TheLoaiCatalogView(topics, true, null);
    }

    public static TheLoaiCatalogView unavailable(String message) {
        return new TheLoaiCatalogView(List.of(), false, message);
    }
}
