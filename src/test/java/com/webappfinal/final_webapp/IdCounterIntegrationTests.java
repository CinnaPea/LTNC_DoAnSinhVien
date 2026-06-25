package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.webappfinal.final_webapp.entity.IdCounter;
import com.webappfinal.final_webapp.repository.IdCounterRepository;
import com.webappfinal.final_webapp.service.IdGenerationService;

@SpringBootTest
class IdCounterIntegrationTests {

    @Autowired
    private IdCounterRepository idCounterRepository;

    @Autowired
    private IdGenerationService idGenerationService;

    @Test
    void existingCountersMatchExpectedPrefixes() {
        IdCounter nguoiDung = idCounterRepository.findById(IdGenerationService.NGUOI_DUNG).orElseThrow();
        IdCounter sinhVien = idCounterRepository.findById(IdGenerationService.SINH_VIEN).orElseThrow();
        IdCounter giangVien = idCounterRepository.findById(IdGenerationService.GIANG_VIEN).orElseThrow();

        assertThat(nguoiDung.getPrefix()).isEqualTo("ND");
        assertThat(sinhVien.getPrefix()).isEqualTo("SV");
        assertThat(giangVien.getPrefix()).isEqualTo("GV");
    }

    @Test
    void previewMethodsReflectCurrentDatabaseCountersWithoutMutatingThem() {
        IdCounter nguoiDung = idCounterRepository.findById(IdGenerationService.NGUOI_DUNG).orElseThrow();
        IdCounter sinhVien = idCounterRepository.findById(IdGenerationService.SINH_VIEN).orElseThrow();
        IdCounter giangVien = idCounterRepository.findById(IdGenerationService.GIANG_VIEN).orElseThrow();

        assertThat(idGenerationService.previewNextNguoiDungId())
            .isEqualTo("ND" + String.format("%04d", nguoiDung.getCurrentValue() + 1));
        assertThat(idGenerationService.previewNextSinhVienId())
            .isEqualTo("SV" + String.format("%04d", sinhVien.getCurrentValue() + 1));
        assertThat(idGenerationService.previewNextGiangVienId())
            .isEqualTo("GV" + String.format("%04d", giangVien.getCurrentValue() + 1));
    }
}
