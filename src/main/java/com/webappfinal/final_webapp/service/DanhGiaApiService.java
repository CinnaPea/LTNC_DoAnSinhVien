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
import com.webappfinal.final_webapp.dto.DanhGiaApiItem;
import com.webappfinal.final_webapp.dto.DanhGiaCatalogView;
import com.webappfinal.final_webapp.dto.DanhGiaForm;

@Service
public class DanhGiaApiService {
    private static final ParameterizedTypeReference<List<DanhGiaApiItem>> EVALUATION_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DanhGiaApiService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.rails.base-url:http://localhost:3000}") String railsBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(trimTrailingSlash(railsBaseUrl)).build();
        this.objectMapper = objectMapper;
    }

    public DanhGiaCatalogView fetchEvaluationsForStudent(String svId) {
        return fetchCatalog("sv_id", svId,
            "Khong the tai danh gia cua sinh vien tu Rails API.");
    }

    public DanhGiaCatalogView fetchEvaluationsForLecturer(String gvId) {
        return fetchCatalog("gv_id", gvId,
            "Khong the tai danh gia cua giang vien tu Rails API.");
    }

    public DanhGiaCatalogView fetchEvaluationsForThesis(String daId) {
        return fetchCatalog("da_id", daId,
            "Khong the tai danh gia cua do an tu Rails API.");
    }

    public void createEvaluation(String daId, String gvId, DanhGiaForm form) {
        try {
            restClient.post()
                .uri("/api/danh-gia")
                .body(Map.of("danh_gia", buildCreatePayload(daId, gvId, form)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new DanhGiaApiException(extractApiMessage(ex, "Khong tao duoc danh gia."));
        } catch (RestClientException ex) {
            throw new DanhGiaApiException("Khong the ket noi Rails API de tao danh gia.");
        }
    }

    public void updateEvaluation(String dgId, DanhGiaForm form) {
        try {
            restClient.patch()
                .uri("/api/danh-gia/{id}", dgId)
                .body(Map.of("danh_gia", buildUpdatePayload(form)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new DanhGiaApiException(extractApiMessage(ex, "Khong cap nhat duoc danh gia."));
        } catch (RestClientException ex) {
            throw new DanhGiaApiException("Khong the ket noi Rails API de cap nhat danh gia.");
        }
    }

    public void deleteEvaluation(String dgId) {
        try {
            restClient.delete()
                .uri("/api/danh-gia/{id}", dgId)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new DanhGiaApiException(extractApiMessage(ex, "Khong xoa duoc danh gia."));
        } catch (RestClientException ex) {
            throw new DanhGiaApiException("Khong the ket noi Rails API de xoa danh gia.");
        }
    }

    private DanhGiaCatalogView fetchCatalog(String filterKey, String filterValue, String unavailableMessage) {
        try {
            List<DanhGiaApiItem> evaluations = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/danh-gia").queryParam(filterKey, filterValue).build())
                .retrieve()
                .body(EVALUATION_LIST_TYPE);
            return DanhGiaCatalogView.available(evaluations);
        } catch (RestClientException ex) {
            return DanhGiaCatalogView.unavailable(unavailableMessage);
        }
    }

    private Map<String, Object> buildCreatePayload(String daId, String gvId, DanhGiaForm form) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("DA_ID", daId);
        payload.put("GV_ID", gvId);
        payload.put("DiemSo", form.getDiemSo());
        payload.put("NhanXet", trim(form.getNhanXet()));
        return payload;
    }

    private Map<String, Object> buildUpdatePayload(DanhGiaForm form) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("DiemSo", form.getDiemSo());
        payload.put("NhanXet", trim(form.getNhanXet()));
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
}
