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
import com.webappfinal.final_webapp.dto.BaiDangApiItem;
import com.webappfinal.final_webapp.dto.BaiDangCatalogView;
import com.webappfinal.final_webapp.dto.BaiDangForm;

@Service
public class BaiDangApiService {
    private static final ParameterizedTypeReference<List<BaiDangApiItem>> SUBMISSION_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public BaiDangApiService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.rails.base-url:http://localhost:3000}") String railsBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(trimTrailingSlash(railsBaseUrl)).build();
        this.objectMapper = objectMapper;
    }

    public BaiDangCatalogView fetchSubmissionsForStudent(String svId) {
        return fetchCatalog("sv_id", svId,
            "Khong the tai bai nop cua sinh vien tu Rails API.");
    }

    public BaiDangCatalogView fetchSubmissionsForLecturer(String gvId) {
        return fetchCatalog("gv_id", gvId,
            "Khong the tai bai nop cua giang vien tu Rails API.");
    }

    public BaiDangCatalogView fetchSubmissionsForThesis(String daId) {
        return fetchCatalog("da_id", daId,
            "Khong the tai bai nop cua do an tu Rails API.");
    }

    public void createSubmission(String daId, BaiDangForm form) {
        try {
            restClient.post()
                .uri("/api/bai-dang")
                .body(Map.of("bai_dang", buildPayload(daId, form)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BaiDangApiException(extractApiMessage(ex, "Khong tao duoc bai nop."));
        } catch (RestClientException ex) {
            throw new BaiDangApiException("Khong the ket noi Rails API de tao bai nop.");
        }
    }

    public void updateSubmission(String bdId, BaiDangForm form) {
        try {
            restClient.patch()
                .uri("/api/bai-dang/{id}", bdId)
                .body(Map.of("bai_dang", buildUpdatePayload(form)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BaiDangApiException(extractApiMessage(ex, "Khong cap nhat duoc bai nop."));
        } catch (RestClientException ex) {
            throw new BaiDangApiException("Khong the ket noi Rails API de cap nhat bai nop.");
        }
    }

    public void deleteSubmission(String bdId) {
        try {
            restClient.delete()
                .uri("/api/bai-dang/{id}", bdId)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BaiDangApiException(extractApiMessage(ex, "Khong xoa duoc bai nop."));
        } catch (RestClientException ex) {
            throw new BaiDangApiException("Khong the ket noi Rails API de xoa bai nop.");
        }
    }

    private BaiDangCatalogView fetchCatalog(String filterKey, String filterValue, String unavailableMessage) {
        try {
            List<BaiDangApiItem> submissions = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/bai-dang").queryParam(filterKey, filterValue).build())
                .retrieve()
                .body(SUBMISSION_LIST_TYPE);
            return BaiDangCatalogView.available(submissions);
        } catch (RestClientException ex) {
            return BaiDangCatalogView.unavailable(unavailableMessage);
        }
    }

    private Map<String, Object> buildPayload(String daId, BaiDangForm form) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("DA_ID", daId);
        payload.put("TieuDe", trim(form.getTieuDe()));
        payload.put("MoTa", trim(form.getMoTa()));
        payload.put("Link", trim(form.getLink()));
        return payload;
    }

    private Map<String, Object> buildUpdatePayload(BaiDangForm form) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("TieuDe", trim(form.getTieuDe()));
        payload.put("MoTa", trim(form.getMoTa()));
        payload.put("Link", trim(form.getLink()));
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
