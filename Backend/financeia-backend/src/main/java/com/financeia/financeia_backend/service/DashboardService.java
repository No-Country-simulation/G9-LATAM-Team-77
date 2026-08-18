package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.dashboard.DashboardResponse;
import com.financeia.financeia_backend.entity.HistorialAnalisis;
import com.financeia.financeia_backend.repository.HistorialAnalisisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final HistorialAnalisisRepository historialAnalisisRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getLatestSummary(Long usuarioId) {
        return historialAnalisisRepository.findFirstByUsuarioIdOrderByFechaDesc(usuarioId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<DashboardResponse> getHistory(Long usuarioId) {
        return historialAnalisisRepository.findAllByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DashboardResponse mapToResponse(HistorialAnalisis entity) {
        return new DashboardResponse(
                entity.getId(),
                entity.getFecha(),
                entity.getIngresoMensual(),
                entity.getNivelEndeudamiento(),
                entity.getFrecuenciaAhorro(),
                entity.getTotalGastos(),
                entity.getAhorroEstimado(),
                entity.getScoreFinanciero(),
                entity.getResumenCategorias()
        );
    }
}
