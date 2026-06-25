package com.webappfinal.final_webapp.dto;

public record InstructorEvaluationView(
        InstructorThesisView thesisView,
        DanhGiaApiItem evaluation) {

    public boolean hasEvaluation() {
        return evaluation != null && evaluation.getDgId() != null && !evaluation.getDgId().isBlank();
    }
}
