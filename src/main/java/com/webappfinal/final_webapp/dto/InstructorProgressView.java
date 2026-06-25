package com.webappfinal.final_webapp.dto;

import java.util.List;

public record InstructorProgressView(
        InstructorThesisView thesisView,
        List<TienDoApiItem> entries) {
}
