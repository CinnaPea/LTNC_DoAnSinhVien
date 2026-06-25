package com.webappfinal.final_webapp.service;

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
import com.webappfinal.final_webapp.dto.DangKyApiItem;
import com.webappfinal.final_webapp.dto.DangKyCatalogView;

@Service
public class DangKyApiService {
    private static final ParameterizedTypeReference<List<DangKyApiItem>> REGISTRATION_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DangKyApiService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.rails.base-url:http://localhost:3000}") String railsBaseUrl) {
        this.restClient = restClientBuilder
            .baseUrl(trimTrailingSlash(railsBaseUrl))
            .build();
        this.objectMapper = objectMapper;
    }

    public DangKyCatalogView fetchRegistrationsForStudent(String svId) {
        try {
            List<DangKyApiItem> registrations = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/dang-ky").queryParam("sv_id", svId).build())
                .retrieve()
                .body(REGISTRATION_LIST_TYPE);

            return DangKyCatalogView.available(registrations);
        } catch (RestClientException ex) {
            return DangKyCatalogView.unavailable(
                "Khong the tai danh sach dang ky tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    public DangKyCatalogView fetchRegistrations() {
        try {
            List<DangKyApiItem> registrations = restClient.get()
                .uri("/api/dang-ky")
                .retrieve()
                .body(REGISTRATION_LIST_TYPE);

            return DangKyCatalogView.available(registrations);
        } catch (RestClientException ex) {
            return DangKyCatalogView.unavailable(
                "Khong the tai toan bo danh sach dang ky tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    public DangKyCatalogView fetchRegistrationsForLecturer(String gvId) {
        try {
            List<DangKyApiItem> registrations = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/dang-ky").queryParam("gv_id", gvId).build())
                .retrieve()
                .body(REGISTRATION_LIST_TYPE);

            return DangKyCatalogView.available(registrations);
        } catch (RestClientException ex) {
            return DangKyCatalogView.unavailable(
                "Khong the tai danh sach dang ky cua giang vien tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    public void createRegistration(String svId, String tlId, String ghiChu) {
        try {
            restClient.post()
                .uri("/api/dang-ky")
                .body(Map.of("dang_ky", Map.of(
                    "SV_ID", svId,
                    "TL_ID", tlId,
                    "GhiChu", trimToEmpty(ghiChu))))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new DangKyApiException(extractApiMessage(ex, "Khong tao duoc dang ky de tai."));
        } catch (RestClientException ex) {
            throw new DangKyApiException("Khong the ket noi Rails API de tao dang ky.");
        }
    }

    public void updateRegistration(String dkId, String ghiChu) {
        try {
            restClient.patch()
                .uri("/api/dang-ky/{id}", dkId)
                .body(Map.of("dang_ky", Map.of("GhiChu", trimToEmpty(ghiChu))))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new DangKyApiException(extractApiMessage(ex, "Khong cap nhat duoc dang ky."));
        } catch (RestClientException ex) {
            throw new DangKyApiException("Khong the ket noi Rails API de cap nhat dang ky.");
        }
    }

    public void deleteRegistration(String dkId) {
        try {
            restClient.delete()
                .uri("/api/dang-ky/{id}", dkId)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new DangKyApiException(extractApiMessage(ex, "Khong xoa duoc dang ky."));
        } catch (RestClientException ex) {
            throw new DangKyApiException("Khong the ket noi Rails API de xoa dang ky.");
        }
    }

    public void approveRegistration(String dkId, String gvId) {
        sendDecision(dkId, gvId, "approve", "Khong duyet duoc dang ky.", "Khong the ket noi Rails API de duyet dang ky.");
    }

    public void rejectRegistration(String dkId, String gvId) {
        sendDecision(dkId, gvId, "reject", "Khong tu choi duoc dang ky.", "Khong the ket noi Rails API de tu choi dang ky.");
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
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

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private void sendDecision(String dkId, String gvId, String action, String fallback, String connectionError) {
        try {
            restClient.patch()
                .uri("/api/dang-ky/{id}/" + action, dkId)
                .body(Map.of("dang_ky", Map.of("NguoiChapThuan", gvId)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new DangKyApiException(extractApiMessage(ex, fallback));
        } catch (RestClientException ex) {
            throw new DangKyApiException(connectionError);
        }
    }
}
