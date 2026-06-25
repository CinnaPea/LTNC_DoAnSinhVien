package com.webappfinal.final_webapp.dto;

import java.util.List;

public record StudentProgressView(
        StudentThesisView thesisView,
        List<TienDoApiItem> entries) {
}
