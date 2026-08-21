package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.historial.HistorialAnalisisResponse;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.service.HistorialAnalisisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historial-analisis")
@RequiredArgsConstructor
public class HistorialAnalisisController {

    private final HistorialAnalisisService historialAnalisisService;

    @GetMapping
    public ResponseEntity<List<HistorialAnalisisResponse>> consultar(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                historialAnalisisService.consultarHistorial(user)
        );
    }
}
