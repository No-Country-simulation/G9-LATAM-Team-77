package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.catalogo.MonedaResponse;
import com.financeia.financeia_backend.dto.catalogo.PaisResponse;
import com.financeia.financeia_backend.entity.Moneda;
import com.financeia.financeia_backend.entity.Pais;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private PaisRepository paisRepository;

    @Mock
    private MonedaRepository monedaRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    @Test
    void shouldMapCountriesWithoutAssumingIdentifiers() {
        Pais honduras = new Pais(47L, "Honduras", "HN");
        when(paisRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre")))
                .thenReturn(List.of(honduras));

        List<PaisResponse> result = catalogoService.findPaises();

        assertEquals(List.of(new PaisResponse(47L, "Honduras", "HN")), result);
    }

    @Test
    void shouldMapCurrenciesWithoutAssumingIdentifiers() {
        Moneda lempira = new Moneda(83L, "Lempira hondureño", "HNL", "L");
        when(monedaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre")))
                .thenReturn(List.of(lempira));

        List<MonedaResponse> result = catalogoService.findMonedas();

        assertEquals(
                List.of(new MonedaResponse(83L, "Lempira hondureño", "HNL", "L")),
                result
        );
    }
}
