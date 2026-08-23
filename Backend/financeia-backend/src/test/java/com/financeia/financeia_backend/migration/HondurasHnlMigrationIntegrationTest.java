package com.financeia.financeia_backend.migration;

import com.financeia.financeia_backend.dto.auth.RegistroRequest;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.UserRepository;
import com.financeia.financeia_backend.service.AuthService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class HondurasHnlMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Flyway flyway;

    @DynamicPropertySource
    static void configureIsolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:honduras_hnl_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
        );
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void debeAplicarV5YRegistrarUsuarioConHondurasYHnl() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("7");

        Long paisId = jdbcTemplate.queryForObject(
                "SELECT id FROM paises WHERE codigo = ?",
                Long.class,
                "HN"
        );
        Long monedaId = jdbcTemplate.queryForObject(
                "SELECT id FROM monedas WHERE codigo = ?",
                Long.class,
                "HNL"
        );

        assertThat(paisId).isNotNull();
        assertThat(monedaId).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM paises WHERE codigo = ?",
                Integer.class,
                "HN"
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT nombre FROM paises WHERE id = ?",
                String.class,
                paisId
        )).isEqualTo("Honduras");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM monedas WHERE codigo = ?",
                Integer.class,
                "HNL"
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT nombre FROM monedas WHERE id = ?",
                String.class,
                monedaId
        )).isEqualTo("Lempira hondureño");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT simbolo FROM monedas WHERE id = ?",
                String.class,
                monedaId
        )).isEqualTo("L");

        authService.register(new RegistroRequest(
                "Usuario Honduras",
                "usuario-honduras@test.com",
                "password-seguro",
                paisId,
                monedaId
        ));

        User saved = userRepository.findByEmail("usuario-honduras@test.com")
                .orElseThrow();

        assertThat(saved.getCountry().getId()).isEqualTo(paisId);
        assertThat(saved.getCountry().getCodigo()).isEqualTo("HN");
        assertThat(saved.getMoneda().getId()).isEqualTo(monedaId);
        assertThat(saved.getMoneda().getCodigo()).isEqualTo("HNL");
    }
}
