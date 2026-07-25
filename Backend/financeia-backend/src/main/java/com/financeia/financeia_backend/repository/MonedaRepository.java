package com.financeia.financeia_backend.repository;

import com.financeia.financeia_backend.entity.Moneda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonedaRepository extends JpaRepository<Moneda, Long> {
}
