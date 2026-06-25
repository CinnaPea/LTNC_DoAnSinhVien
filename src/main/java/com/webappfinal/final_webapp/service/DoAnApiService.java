package com.webappfinal.final_webapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.webappfinal.final_webapp.dto.DoAnApiItem;
import com.webappfinal.final_webapp.dto.DoAnCatalogView;

@Service
public class DoAnApiService {
    private static final ParameterizedTypeReference<List<DoAnApiItem>> THESIS_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public DoAnApiService(
            RestClient.Builder restClientBuilder,
            @Value("${app.rails.base-url:http://localhost:3000}") String railsBaseUrl) {
        this.restClient = restClientBuilder
            .baseUrl(trimTrailingSlash(railsBaseUrl))
            .build();
    }

    public DoAnCatalogView fetchThesesForStudent(String svId) {
        try {
            List<DoAnApiItem> theses = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/do-an").queryParam("sv_id", svId).build())
                .retrieve()
                .body(THESIS_LIST_TYPE);

            return DoAnCatalogView.available(theses);
        } catch (RestClientException ex) {
            return DoAnCatalogView.unavailable(
                "Khong the tai danh sach do an tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    public DoAnCatalogView fetchTheses() {
        try {
            List<DoAnApiItem> theses = restClient.get()
                .uri("/api/do-an")
                .retrieve()
                .body(THESIS_LIST_TYPE);

            return DoAnCatalogView.available(theses);
        } catch (RestClientException ex) {
            return DoAnCatalogView.unavailable(
                "Khong the tai toan bo danh sach do an tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    public DoAnCatalogView fetchThesesForLecturer(String gvId) {
        try {
            List<DoAnApiItem> theses = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/do-an").queryParam("gv_id", gvId).build())
                .retrieve()
                .body(THESIS_LIST_TYPE);

            return DoAnCatalogView.available(theses);
        } catch (RestClientException ex) {
            return DoAnCatalogView.unavailable(
                "Khong the tai danh sach do an cua giang vien tu Rails API. Vui long kiem tra server Ruby on Rails.");
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
