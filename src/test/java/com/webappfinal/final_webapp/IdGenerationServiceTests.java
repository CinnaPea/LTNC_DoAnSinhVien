package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webappfinal.final_webapp.entity.IdCounter;
import com.webappfinal.final_webapp.repository.IdCounterRepository;
import com.webappfinal.final_webapp.service.IdGenerationService;

@ExtendWith(MockitoExtension.class)
class IdGenerationServiceTests {
    @Mock
    private IdCounterRepository idCounterRepository;

    @Test
    void previewsNextNguoiDungIdFromCounter() {
        when(idCounterRepository.findById(IdGenerationService.NGUOI_DUNG))
            .thenReturn(Optional.of(counter(IdGenerationService.NGUOI_DUNG, "ND", 2)));

        IdGenerationService service = new IdGenerationService(idCounterRepository);

        assertThat(service.previewNextNguoiDungId()).isEqualTo("ND0003");
    }

    @Test
    void allocatesAndPersistsIncrementedCounterValue() {
        IdCounter counter = counter(IdGenerationService.SINH_VIEN, "SV", 1);
        when(idCounterRepository.findByEntityNameForUpdate(IdGenerationService.SINH_VIEN))
            .thenReturn(Optional.of(counter));

        IdGenerationService service = new IdGenerationService(idCounterRepository);

        assertThat(service.allocateNextId(IdGenerationService.SINH_VIEN)).isEqualTo("SV0002");
        assertThat(counter.getCurrentValue()).isEqualTo(2);
        verify(idCounterRepository).save(counter);
    }

    @Test
    void throwsWhenCounterRowIsMissing() {
        when(idCounterRepository.findById("MISSING")).thenReturn(Optional.empty());

        IdGenerationService service = new IdGenerationService(idCounterRepository);

        assertThatThrownBy(() -> service.previewNextId("MISSING"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unknown counter: MISSING");
    }

    private IdCounter counter(String entityName, String prefix, int currentValue) {
        IdCounter counter = new IdCounter();
        counter.setEntityName(entityName);
        counter.setPrefix(prefix);
        counter.setCurrentValue(currentValue);
        return counter;
    }
}
