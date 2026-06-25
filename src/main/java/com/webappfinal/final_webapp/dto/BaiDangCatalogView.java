package com.webappfinal.final_webapp.dto;

import java.util.List;

public record BaiDangCatalogView(
        List<BaiDangApiItem> submissions,
        boolean apiAvailable,
        String message) {

    public static BaiDangCatalogView available(List<BaiDangApiItem> submissions) {
        return new BaiDangCatalogView(submissions == null ? List.of() : List.copyOf(submissions), true, null);
    }

    public static BaiDangCatalogView unavailable(String message) {
        return new BaiDangCatalogView(List.of(), false, message);
    }
}
