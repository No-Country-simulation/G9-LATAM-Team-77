package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.catalogo.MonedaResponse;
import com.financeia.financeia_backend.dto.catalogo.PaisResponse;
import com.financeia.financeia_backend.config.SecurityConfig;
import com.financeia.financeia_backend.repository.UserRepository;
import com.financeia.financeia_backend.service.CatalogoService;
import com.financeia.financeia_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogoController.class)
@Import(SecurityConfig.class)
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldExposeCountriesWithoutAuthentication() throws Exception {
        when(catalogoService.findPaises()).thenReturn(List.of(
                new PaisResponse(9L, "Honduras", "HN")
        ));

        mockMvc.perform(get("/api/v1/catalogos/paises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].nombre").value("Honduras"))
                .andExpect(jsonPath("$[0].codigo").value("HN"));
    }

    @Test
    void shouldExposeCurrenciesWithoutAuthentication() throws Exception {
        when(catalogoService.findMonedas()).thenReturn(List.of(
                new MonedaResponse(12L, "Lempira hondureño", "HNL", "L")
        ));

        mockMvc.perform(get("/api/v1/catalogos/monedas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].nombre").value("Lempira hondureño"))
                .andExpect(jsonPath("$[0].codigo").value("HNL"))
                .andExpect(jsonPath("$[0].simbolo").value("L"));
    }

    @Test
    void shouldNotExposeCatalogWriteOperations() throws Exception {
        mockMvc.perform(post("/api/v1/catalogos/paises"))
                .andExpect(status().isMethodNotAllowed());
    }
}
