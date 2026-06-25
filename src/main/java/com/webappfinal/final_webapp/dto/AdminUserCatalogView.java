package com.webappfinal.final_webapp.dto;

import java.util.List;

public record AdminUserCatalogView(
        List<AdminUserApiItem> users,
        boolean apiAvailable,
        String message) {

    public static AdminUserCatalogView available(List<AdminUserApiItem> users) {
        return new AdminUserCatalogView(users == null ? List.of() : users, true, null);
    }

    public static AdminUserCatalogView unavailable(String message) {
        return new AdminUserCatalogView(List.of(), false, message);
    }
}
