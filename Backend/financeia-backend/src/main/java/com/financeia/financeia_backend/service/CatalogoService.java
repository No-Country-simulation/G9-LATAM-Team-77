package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.catalogo.MonedaResponse;
import com.financeia.financeia_backend.dto.catalogo.PaisResponse;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final PaisRepository paisRepository;
    private final MonedaRepository monedaRepository;

    @Transactional(readOnly = true)
    public List<PaisResponse> findPaises() {
        return paisRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"))
                .stream()
                .map(pais -> new PaisResponse(
                        pais.getId(),
                        pais.getNombre(),
                        pais.getCodigo()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonedaResponse> findMonedas() {
        return monedaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"))
                .stream()
                .map(moneda -> new MonedaResponse(
                        moneda.getId(),
                        moneda.getNombre(),
                        moneda.getCodigo(),
                        moneda.getSimbolo()
                ))
                .toList();
    }
}
