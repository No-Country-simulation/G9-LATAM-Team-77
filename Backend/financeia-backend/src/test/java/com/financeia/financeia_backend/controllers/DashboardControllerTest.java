package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.dashboard.DashboardResponse;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.UserRepository;
import com.financeia.financeia_backend.service.DashboardService;
import com.financeia.financeia_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldReturnDashboardSummary() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@test.com");

        DashboardResponse response = new DashboardResponse(
                new BigDecimal("850000"),
                new BigDecimal("520000"),
                new BigDecimal("330000"),

                new BigDecimal("800000"),
                new BigDecimal("450000"),
                new BigDecimal("350000"),

                new BigDecimal("15.56"),

                Map.of(
                        "Alimentación",
                        new BigDecimal("180000"),
                        "Transporte",
                        new BigDecimal("90000")
                )
        );

        when(dashboardService.getSummary(user))
                .thenReturn(response);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        mockMvc.perform(
                        get("/api/v1/dashboard/summary")
                                .with(
                                        SecurityMockMvcRequestPostProcessors.authentication(
                                                authentication
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresosMesActual").value(850000))
                .andExpect(jsonPath("$.gastosMesActual").value(520000))
                .andExpect(jsonPath("$.balanceMesActual").value(330000))
                .andExpect(jsonPath("$.ingresosMesAnterior").value(800000))
                .andExpect(jsonPath("$.gastosMesAnterior").value(450000))
                .andExpect(jsonPath("$.balanceMesAnterior").value(350000))
                .andExpect(jsonPath("$.variacionGastos").value(15.56))
                .andExpect(jsonPath("$.gastosPorCategoria.Alimentación").value(180000))
                .andExpect(jsonPath("$.gastosPorCategoria.Transporte").value(90000));
    }
}