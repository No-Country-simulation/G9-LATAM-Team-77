package com.financeia.financeia_backend.repository;

import com.financeia.financeia_backend.entity.HistorialAnalisis;
import com.financeia.financeia_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialAnalisisRepository extends JpaRepository<HistorialAnalisis, Long> {

    List<HistorialAnalisis> findAllByUserOrderByFechaHoraDesc(User user);
}
