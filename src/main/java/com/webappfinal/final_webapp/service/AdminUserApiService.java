package com.webappfinal.final_webapp.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webappfinal.final_webapp.dto.AdminUserApiItem;
import com.webappfinal.final_webapp.dto.AdminUserCatalogView;
import com.webappfinal.final_webapp.dto.AdminUserForm;

@Service
public class AdminUserApiService {
    private static final ParameterizedTypeReference<List<AdminUserApiItem>> USER_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AdminUserApiService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.rails.base-url:http://localhost:3000}") String railsBaseUrl) {
        this.restClient = restClientBuilder
            .baseUrl(trimTrailingSlash(railsBaseUrl))
            .build();
        this.objectMapper = objectMapper;
    }

    public AdminUserCatalogView fetchUsers() {
        try {
            List<AdminUserApiItem> users = restClient.get()
                .uri("/api/admin/users")
                .retrieve()
                .body(USER_LIST_TYPE);

            return AdminUserCatalogView.available(users);
        } catch (RestClientException ex) {
            return AdminUserCatalogView.unavailable(
                "Khong the tai danh sach nguoi dung tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    public AdminUserApiItem fetchUser(String ndId) {
        try {
            return restClient.get()
                .uri("/api/admin/users/{id}", ndId)
                .retrieve()
                .body(AdminUserApiItem.class);
        } catch (RestClientResponseException ex) {
            throw new AdminUserApiException(extractApiMessage(ex, "Khong tai duoc thong tin nguoi dung."));
        } catch (RestClientException ex) {
            throw new AdminUserApiException("Khong the ket noi Rails API de tai nguoi dung.");
        }
    }

    public AdminUserApiItem createUser(AdminUserForm form) {
        try {
            return restClient.post()
                .uri("/api/admin/users")
                .body(Map.of("user", buildUserPayload(form, true)))
                .retrieve()
                .body(AdminUserApiItem.class);
        } catch (RestClientResponseException ex) {
            throw new AdminUserApiException(extractApiMessage(ex, "Khong tao duoc nguoi dung."));
        } catch (RestClientException ex) {
            throw new AdminUserApiException("Khong the ket noi Rails API de tao nguoi dung.");
        }
    }

    public AdminUserApiItem updateUser(String ndId, AdminUserForm form) {
        try {
            return restClient.patch()
                .uri("/api/admin/users/{id}", ndId)
                .body(Map.of("user", buildUserPayload(form, false)))
                .retrieve()
                .body(AdminUserApiItem.class);
        } catch (RestClientResponseException ex) {
            throw new AdminUserApiException(extractApiMessage(ex, "Khong cap nhat duoc nguoi dung."));
        } catch (RestClientException ex) {
            throw new AdminUserApiException("Khong the ket noi Rails API de cap nhat nguoi dung.");
        }
    }

    public void updateStatus(String ndId, boolean active) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("TrangThai", active);

            restClient.patch()
                .uri("/api/admin/users/{id}", ndId)
                .body(Map.of("user", payload))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new AdminUserApiException(extractApiMessage(ex, "Khong cap nhat duoc trang thai nguoi dung."));
        } catch (RestClientException ex) {
            throw new AdminUserApiException("Khong the ket noi Rails API de cap nhat nguoi dung.");
        }
    }

    public void deleteUser(String ndId) {
        try {
            restClient.delete()
                .uri("/api/admin/users/{id}", ndId)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new AdminUserApiException(extractApiMessage(ex, "Khong xoa duoc nguoi dung."));
        } catch (RestClientException ex) {
            throw new AdminUserApiException("Khong the ket noi Rails API de xoa nguoi dung.");
        }
    }

    private Map<String, Object> buildUserPayload(AdminUserForm form, boolean includeRole) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("Username", trim(form.getUsername()));
        payload.put("Email", trim(form.getEmail()));
        payload.put("TrangThai", form.isActive());
        payload.put("ProfileName", trim(form.getProfileName()));

        if (includeRole) {
            payload.put("VT_ID", trim(form.getVaiTroId()));
        }
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            payload.put("Password", form.getPassword());
        }
        return payload;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String extractApiMessage(RestClientResponseException ex, String fallback) {
        try {
            Map<String, Object> body = objectMapper.readValue(ex.getResponseBodyAsByteArray(), new TypeReference<>() { });
            Object errors = body.get("errors");
            if (errors instanceof List<?> errorList && !errorList.isEmpty()) {
                return errorList.stream().map(String::valueOf).reduce((a, b) -> a + " " + b).orElse(fallback);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }
}
