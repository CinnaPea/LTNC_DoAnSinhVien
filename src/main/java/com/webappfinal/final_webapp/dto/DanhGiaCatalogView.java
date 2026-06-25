package com.webappfinal.final_webapp.dto;

import java.util.List;

public record DanhGiaCatalogView(
        List<DanhGiaApiItem> evaluations,
        boolean apiAvailable,
        String message) {

    public static DanhGiaCatalogView available(List<DanhGiaApiItem> evaluations) {
        return new DanhGiaCatalogView(evaluations == null ? List.of() : List.copyOf(evaluations), true, null);
    }

    public static DanhGiaCatalogView unavailable(String message) {
        return new DanhGiaCatalogView(List.of(), false, message);
    }
}
