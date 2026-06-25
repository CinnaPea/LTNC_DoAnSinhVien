package com.webappfinal.final_webapp.dto;

import java.util.List;

public record DangKyCatalogView(
        List<DangKyApiItem> registrations,
        boolean apiAvailable,
        String message) {

    public static DangKyCatalogView available(List<DangKyApiItem> registrations) {
        return new DangKyCatalogView(registrations == null ? List.of() : List.copyOf(registrations), true, null);
    }

    public static DangKyCatalogView unavailable(String message) {
        return new DangKyCatalogView(List.of(), false, message);
    }
}
