package com.financeia.financeia_backend.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PasswordResetMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureIsolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:password_reset_migration;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
        );
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void appliesFlywayFromV1ThroughV7AndPreservesSecureTokenTable() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("7");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_NAME) = 'password_reset_tokens'",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE LOWER(TABLE_NAME) = 'password_reset_tokens' AND LOWER(COLUMN_NAME) = 'token_hash'",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE LOWER(TABLE_NAME) = 'usuarios' AND LOWER(COLUMN_NAME) = 'google_subject'",
                Integer.class
        )).isEqualTo(1);
    }
}
