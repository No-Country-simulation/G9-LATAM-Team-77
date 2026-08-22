package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import com.financeia.financeia_backend.dto.analisis.TransaccionAnalisisRequest;
import com.financeia.financeia_backend.entity.HistorialAnalisis;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.HistorialAnalisisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistorialAnalisisServiceTest {

    private HistorialAnalisisRepository repository;
    private HistorialAnalisisService service;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        repository = mock(HistorialAnalisisRepository.class);
        jsonMapper = new JsonMapper();
        service = new HistorialAnalisisService(repository, jsonMapper);
    }

    @Test
    void debePersistirTodosLosDatosDelAnalisisExitoso() throws Exception {
        User user = new User();
        user.setId(7L);
        AnalisisDataScienceRequest request = new AnalisisDataScienceRequest(
                new BigDecimal("4500.00"),
                new BigDecimal("25.00"),
                "Media",
                List.of(new TransaccionAnalisisRequest("Transporte", new BigDecimal("420.00")))
        );
        JsonNode response = jsonMapper.readTree("""
                {
                  "status":"success",
                  "perfil_financiero":"Saludable",
                  "probabilidad":0.96,
                  "total_gastos":420.00,
                  "ahorro_estimado":4080.00,
                  "resumen_gastos":{"Transporte":420.00},
                  "recomendaciones":["Mantener hábitos saludables"]
                }
                """);

        service.registrarAnalisisExitoso(user, request, response);

        ArgumentCaptor<HistorialAnalisis> captor = ArgumentCaptor.forClass(HistorialAnalisis.class);
        verify(repository).save(captor.capture());
        HistorialAnalisis saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getFechaHora()).isNotNull();
        assertThat(saved.getPerfilFinanciero()).isEqualTo("Saludable");
        assertThat(saved.getProbabilidad()).isEqualByComparingTo("0.96");
        assertThat(saved.getIngresoMensual()).isEqualByComparingTo("4500.00");
        assertThat(saved.getTotalGastos()).isEqualByComparingTo("420.00");
        assertThat(saved.getAhorroEstimado()).isEqualByComparingTo("4080.00");
        assertThat(saved.getNivelEndeudamiento()).isEqualByComparingTo("25.00");
        assertThat(saved.getFrecuenciaAhorro()).isEqualTo("Media");
        assertThat(saved.getResumenGastos()).contains("Transporte");
        assertThat(saved.getRecomendaciones()).contains("Mantener hábitos saludables");
    }

    @Test
    void noDebePersistirRespuestaQueNoSeaExitosa() throws Exception {
        User user = new User();
        AnalisisDataScienceRequest request = new AnalisisDataScienceRequest(
                BigDecimal.ONE,
                BigDecimal.ZERO,
                "Nunca",
                List.of()
        );

        service.registrarAnalisisExitoso(
                user,
                request,
                jsonMapper.readTree("{\"error\":\"modelo no disponible\"}")
        );

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void debeConsultarRepositorioUsandoSoloElUsuarioAutenticado() {
        User userA = new User();
        userA.setId(1L);
        User userB = new User();
        userB.setId(2L);
        when(repository.findAllByUserOrderByFechaHoraDesc(userA)).thenReturn(List.of());

        service.consultarHistorial(userA);

        verify(repository).findAllByUserOrderByFechaHoraDesc(userA);
        verify(repository, never()).findAllByUserOrderByFechaHoraDesc(userB);
    }
}
