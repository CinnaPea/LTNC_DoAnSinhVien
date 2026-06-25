package com.webappfinal.final_webapp.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.webappfinal.final_webapp.dto.TheLoaiApiItem;
import com.webappfinal.final_webapp.dto.TheLoaiCatalogView;
import com.webappfinal.final_webapp.dto.TheLoaiForm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TheLoaiApiService {
    private static final ParameterizedTypeReference<List<TheLoaiApiItem>> TOPIC_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TheLoaiApiService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.rails.base-url:http://localhost:3000}") String railsBaseUrl) {
        this.restClient = restClientBuilder
            .baseUrl(trimTrailingSlash(railsBaseUrl))
            .build();
        this.objectMapper = objectMapper;
    }

    public TheLoaiCatalogView fetchTopics() {
        try {
            List<TheLoaiApiItem> topics = restClient.get()
                .uri("/api/the-loai")
                .retrieve()
                .body(TOPIC_LIST_TYPE);

            return TheLoaiCatalogView.available(topics == null ? List.of() : topics);
        } catch (RestClientException ex) {
            return TheLoaiCatalogView.unavailable(
                "Khong the tai du lieu de tai tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    public TheLoaiCatalogView fetchTopicsForLecturer(String gvId) {
        try {
            List<TheLoaiApiItem> topics = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/the-loai").queryParam("gv_id", gvId).build())
                .retrieve()
                .body(TOPIC_LIST_TYPE);

            return TheLoaiCatalogView.available(topics == null ? List.of() : topics);
        } catch (RestClientException ex) {
            return TheLoaiCatalogView.unavailable(
                "Khong the tai danh sach de tai cua giang vien tu Rails API.");
        }
    }

    public Optional<TheLoaiApiItem> fetchTopic(String tlId) {
        try {
            TheLoaiApiItem topic = restClient.get()
                .uri("/api/the-loai/{id}", tlId)
                .retrieve()
                .body(TheLoaiApiItem.class);

            return Optional.ofNullable(topic);
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    public void createTopic(TheLoaiForm form, String gvId) {
        try {
            restClient.post()
                .uri("/api/the-loai")
                .body(Map.of("the_loai", buildCreatePayload(form, gvId)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new TheLoaiApiException(extractApiMessage(ex, "Khong tao duoc de tai moi."));
        } catch (RestClientException ex) {
            throw new TheLoaiApiException("Khong the ket noi Rails API de tao de tai.");
        }
    }

    public void updateTopic(String tlId, TheLoaiForm form, String gvId) {
        try {
            restClient.patch()
                .uri("/api/the-loai/{id}", tlId)
                .body(Map.of("the_loai", buildUpdatePayload(form, gvId)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new TheLoaiApiException(extractApiMessage(ex, "Khong cap nhat duoc de tai."));
        } catch (RestClientException ex) {
            throw new TheLoaiApiException("Khong the ket noi Rails API de cap nhat de tai.");
        }
    }

    public void deleteTopic(String tlId) {
        try {
            restClient.delete()
                .uri("/api/the-loai/{id}", tlId)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new TheLoaiApiException(extractApiMessage(ex, "Khong xoa duoc de tai."));
        } catch (RestClientException ex) {
            throw new TheLoaiApiException("Khong the ket noi Rails API de xoa de tai.");
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private Map<String, Object> buildCreatePayload(TheLoaiForm form, String gvId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("MaTL", trim(form.getMaTl()));
        payload.put("TenTL", trim(form.getTenTl()));
        payload.put("MoTa", trim(form.getMoTa()));
        payload.put("GV_ID", gvId);
        payload.put("TrangThai", trim(form.getTrangThai()));
        payload.put("NgayLap", OffsetDateTime.now().toString());
        return payload;
    }

    private Map<String, Object> buildUpdatePayload(TheLoaiForm form, String gvId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("MaTL", trim(form.getMaTl()));
        payload.put("TenTL", trim(form.getTenTl()));
        payload.put("MoTa", trim(form.getMoTa()));
        payload.put("GV_ID", gvId);
        payload.put("TrangThai", trim(form.getTrangThai()));
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
