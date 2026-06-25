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
import com.webappfinal.final_webapp.dto.TienDoApiItem;
import com.webappfinal.final_webapp.dto.TienDoCatalogView;
import com.webappfinal.final_webapp.dto.TienDoForm;

@Service
public class TienDoApiService {
    private static final ParameterizedTypeReference<List<TienDoApiItem>> PROGRESS_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TienDoApiService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.rails.base-url:http://localhost:3000}") String railsBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(trimTrailingSlash(railsBaseUrl)).build();
        this.objectMapper = objectMapper;
    }

    public TienDoCatalogView fetchProgressForStudent(String svId) {
        return fetchCatalog("sv_id", svId,
            "Khong the tai tien do cua sinh vien tu Rails API.");
    }

    public TienDoCatalogView fetchProgressForLecturer(String gvId) {
        return fetchCatalog("gv_id", gvId,
            "Khong the tai tien do cua giang vien tu Rails API.");
    }

    public TienDoCatalogView fetchProgressForThesis(String daId) {
        return fetchCatalog("da_id", daId,
            "Khong the tai tien do cua do an tu Rails API.");
    }

    public void createProgress(String daId, TienDoForm form) {
        try {
            restClient.post()
                .uri("/api/tien-do")
                .body(Map.of("tien_do", buildStudentPayload(daId, form)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new TienDoApiException(extractApiMessage(ex, "Khong tao duoc tien do."));
        } catch (RestClientException ex) {
            throw new TienDoApiException("Khong the ket noi Rails API de tao tien do.");
        }
    }

    public void updateProgress(String tdId, TienDoForm form) {
        try {
            restClient.patch()
                .uri("/api/tien-do/{id}", tdId)
                .body(Map.of("tien_do", buildStudentUpdatePayload(form)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new TienDoApiException(extractApiMessage(ex, "Khong cap nhat duoc tien do."));
        } catch (RestClientException ex) {
            throw new TienDoApiException("Khong the ket noi Rails API de cap nhat tien do.");
        }
    }

    public void updateFeedback(String tdId, String nhanXet) {
        try {
            restClient.patch()
                .uri("/api/tien-do/{id}", tdId)
                .body(Map.of("tien_do", Map.of("NhanXet", trimToEmpty(nhanXet))))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new TienDoApiException(extractApiMessage(ex, "Khong cap nhat duoc nhan xet tien do."));
        } catch (RestClientException ex) {
            throw new TienDoApiException("Khong the ket noi Rails API de cap nhat nhan xet tien do.");
        }
    }

    public void deleteProgress(String tdId) {
        try {
            restClient.delete()
                .uri("/api/tien-do/{id}", tdId)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new TienDoApiException(extractApiMessage(ex, "Khong xoa duoc tien do."));
        } catch (RestClientException ex) {
            throw new TienDoApiException("Khong the ket noi Rails API de xoa tien do.");
        }
    }

    private TienDoCatalogView fetchCatalog(String filterKey, String filterValue, String unavailableMessage) {
        try {
            List<TienDoApiItem> entries = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/tien-do").queryParam(filterKey, filterValue).build())
                .retrieve()
                .body(PROGRESS_LIST_TYPE);
            return TienDoCatalogView.available(entries);
        } catch (RestClientException ex) {
            return TienDoCatalogView.unavailable(unavailableMessage);
        }
    }

    private Map<String, Object> buildStudentPayload(String daId, TienDoForm form) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("DA_ID", daId);
        payload.put("TieuDe", trim(form.getTieuDe()));
        payload.put("NoiDung", trim(form.getNoiDung()));
        payload.put("TienDoHienTai", form.getTienDoHienTai());
        return payload;
    }

    private Map<String, Object> buildStudentUpdatePayload(TienDoForm form) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("TieuDe", trim(form.getTieuDe()));
        payload.put("NoiDung", trim(form.getNoiDung()));
        payload.put("TienDoHienTai", form.getTienDoHienTai());
        return payload;
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

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
