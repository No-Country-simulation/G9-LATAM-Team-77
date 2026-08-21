package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.historial.HistorialAnalisisResponse;
import com.financeia.financeia_backend.entity.Role;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.UserRepository;
import com.financeia.financeia_backend.service.HistorialAnalisisService;
import com.financeia.financeia_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistorialAnalisisController.class)
class HistorialAnalisisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private HistorialAnalisisService historialAnalisisService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void debeRetornar401SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/historial-analisis"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debeRetornar401ConTokenInvalido() throws Exception {
        mockMvc.perform(
                        get("/api/v1/historial-analisis")
                                .header("Authorization", "Bearer token-invalido")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debeRetornarListaVaciaCuandoElUsuarioNoTieneHistorial() throws Exception {
        User user = user(1L, "sin-historial@test.com");
        when(historialAnalisisService.consultarHistorial(user)).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/historial-analisis")
                                .with(authentication(authenticationFor(user)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void debeIgnorarIdentificadorAjenoYConsultarSoloPrincipalAutenticado() throws Exception {
        User userA = user(1L, "usuario-a@test.com");
        HistorialAnalisisResponse item = new HistorialAnalisisResponse(
                10L,
                Instant.parse("2026-08-20T12:00:00Z"),
                "Saludable",
                new BigDecimal("0.960000"),
                new BigDecimal("4500.00"),
                new BigDecimal("920.00"),
                new BigDecimal("3580.00"),
                new BigDecimal("25.00"),
                "Media",
                jsonMapper.readTree("{\"Transporte\":420}"),
                jsonMapper.readTree("[\"Mantener hábitos saludables\"]")
        );

        when(historialAnalisisService.consultarHistorial(userA)).thenReturn(List.of(item));

        mockMvc.perform(
                        get("/api/v1/historial-analisis")
                                .queryParam("usuarioId", "2")
                                .with(authentication(authenticationFor(userA)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].perfilFinanciero").value("Saludable"))
                .andExpect(jsonPath("$[0].resumenGastos.Transporte").value(420));

        verify(historialAnalisisService).consultarHistorial(userA);
    }

    private UsernamePasswordAuthenticationToken authenticationFor(User user) {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getRole().getAuthorities()
        );
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("test-password");
        user.setRole(Role.USER);
        return user;
    }
}
