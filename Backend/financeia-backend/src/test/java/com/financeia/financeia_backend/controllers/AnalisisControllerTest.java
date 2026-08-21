package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import com.financeia.financeia_backend.entity.Role;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.UserRepository;
import com.financeia.financeia_backend.service.DataScienceService;
import com.financeia.financeia_backend.service.HistorialAnalisisService;
import com.financeia.financeia_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalisisController.class)
class AnalisisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private DataScienceService dataScienceService;

    @MockitoBean
    private HistorialAnalisisService historialAnalisisService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void debeRechazarAnalisisSinAutenticacion() throws Exception {
        mockMvc.perform(
                        post("/api/v1/analisis-financiero")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debeRetornarMismoContratoYPersistirAnalisis() throws Exception {
        User user = authenticatedUser(1L, "analisis@test.com");
        JsonNode response = jsonMapper.readTree(responseJson());

        when(dataScienceService.analizar(any(AnalisisDataScienceRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/analisis-financiero")
                                .with(authentication(authenticationFor(user)))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.perfil_financiero").value("Saludable"))
                .andExpect(jsonPath("$.probabilidad").value(0.95))
                .andExpect(jsonPath("$.ingreso_mensual").value(4500.0));

        verify(historialAnalisisService).registrarAnalisisExitoso(
                org.mockito.ArgumentMatchers.eq(user),
                any(AnalisisDataScienceRequest.class),
                org.mockito.ArgumentMatchers.eq(response)
        );
    }

    private UsernamePasswordAuthenticationToken authenticationFor(User user) {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getRole().getAuthorities()
        );
    }

    private User authenticatedUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("test-password");
        user.setRole(Role.USER);
        return user;
    }

    private String requestJson() {
        return """
                {
                  "ingreso_mensual": 4500,
                  "nivel_endeudamiento": 25,
                  "frecuencia_ahorro": "Media",
                  "transacciones": [
                    {"descripcion": "Supermercado", "valor": 420},
                    {"descripcion": "Gasolina", "valor": 250}
                  ]
                }
                """;
    }

    private String responseJson() {
        return """
                {
                  "status": "success",
                  "perfil_financiero": "Saludable",
                  "probabilidad": 0.95,
                  "ingreso_mensual": 4500.0,
                  "total_gastos": 670.0,
                  "ahorro_estimado": 3830.0,
                  "nivel_endeudamiento": 25.0,
                  "frecuencia_ahorro": "Media",
                  "resumen_gastos": {"Supermercado": 420.0, "Gasolina": 250.0},
                  "recomendaciones": ["Mantener hábitos financieros saludables"]
                }
                """;
    }
}
