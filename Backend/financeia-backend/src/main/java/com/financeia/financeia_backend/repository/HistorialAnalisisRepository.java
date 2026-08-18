package com.financeia.financeia_backend.repository;

import com.financeia.financeia_backend.entity.HistorialAnalisis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialAnalisisRepository extends JpaRepository<HistorialAnalisis, Long> {
    Optional<HistorialAnalisis> findFirstByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<HistorialAnalisis> findAllByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
