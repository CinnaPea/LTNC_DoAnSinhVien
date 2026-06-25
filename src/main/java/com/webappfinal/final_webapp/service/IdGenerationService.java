package com.webappfinal.final_webapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webappfinal.final_webapp.entity.IdCounter;
import com.webappfinal.final_webapp.repository.IdCounterRepository;

@Service
public class IdGenerationService {
    public static final String NGUOI_DUNG = "NGUOIDUNG";
    public static final String SINH_VIEN = "SINHVIEN";
    public static final String GIANG_VIEN = "GIANGVIEN";

    private static final int NUMBER_WIDTH = 4;

    private final IdCounterRepository idCounterRepository;

    public IdGenerationService(IdCounterRepository idCounterRepository) {
        this.idCounterRepository = idCounterRepository;
    }

    @Transactional(readOnly = true)
    public String previewNextId(String entityName) {
        IdCounter counter = getCounter(entityName);
        return formatId(counter.getPrefix(), counter.getCurrentValue() + 1);
    }

    @Transactional
    public String allocateNextId(String entityName) {
        IdCounter counter = idCounterRepository.findByEntityNameForUpdate(entityName)
            .orElseThrow(() -> new IllegalArgumentException("Unknown counter: " + entityName));

        int nextValue = counter.getCurrentValue() + 1;
        counter.setCurrentValue(nextValue);
        idCounterRepository.save(counter);

        return formatId(counter.getPrefix(), nextValue);
    }

    public String previewNextNguoiDungId() {
        return previewNextId(NGUOI_DUNG);
    }

    public String previewNextSinhVienId() {
        return previewNextId(SINH_VIEN);
    }

    public String previewNextGiangVienId() {
        return previewNextId(GIANG_VIEN);
    }

    private IdCounter getCounter(String entityName) {
        return idCounterRepository.findById(entityName)
            .orElseThrow(() -> new IllegalArgumentException("Unknown counter: " + entityName));
    }

    static String formatId(String prefix, int value) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Counter prefix must not be blank");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Counter value must not be negative");
        }
        return prefix + String.format("%0" + NUMBER_WIDTH + "d", value);
    }
}
