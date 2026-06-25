package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;

@SpringBootTest
class DatabaseUserLookupIntegrationTests {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Test
    void canLookUpAnExistingUserByUsername() {
        NguoiDung existingUser = nguoiDungRepository.findAll(PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .orElseThrow();

        NguoiDung user = nguoiDungRepository.findByUsername(existingUser.getUsername()).orElseThrow();

        assertThat(user.getUsername()).isEqualTo(existingUser.getUsername());
        assertThat(user.getNdId()).isEqualTo(existingUser.getNdId());
        assertThat(user.getPassHash()).isNotBlank();
    }
}
