package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class DatabaseConnectionIntegrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void connectsToDoAnSinhVienDatabase() {
        String databaseName = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
        assertThat(databaseName).isEqualTo("DoAnSinhVien");
    }
}
