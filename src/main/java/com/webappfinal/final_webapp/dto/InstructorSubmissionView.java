package com.webappfinal.final_webapp.dto;

import java.util.List;

public record InstructorSubmissionView(
        InstructorThesisView thesisView,
        List<BaiDangApiItem> submissions) {
}
