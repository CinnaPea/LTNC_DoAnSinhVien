package com.webappfinal.final_webapp.dto;

import java.util.List;

public record DoAnCatalogView(
        List<DoAnApiItem> theses,
        boolean apiAvailable,
        String message) {

    public static DoAnCatalogView available(List<DoAnApiItem> theses) {
        return new DoAnCatalogView(theses == null ? List.of() : List.copyOf(theses), true, null);
    }

    public static DoAnCatalogView unavailable(String message) {
        return new DoAnCatalogView(List.of(), false, message);
    }
}
