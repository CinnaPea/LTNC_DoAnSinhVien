package com.webappfinal.final_webapp.dto;

import java.util.List;

public record TienDoCatalogView(
        List<TienDoApiItem> entries,
        boolean apiAvailable,
        String message) {

    public static TienDoCatalogView available(List<TienDoApiItem> entries) {
        return new TienDoCatalogView(entries == null ? List.of() : List.copyOf(entries), true, null);
    }

    public static TienDoCatalogView unavailable(String message) {
        return new TienDoCatalogView(List.of(), false, message);
    }
}
