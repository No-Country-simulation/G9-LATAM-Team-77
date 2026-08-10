package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.analisis.AnalisisRequest;
import com.financeia.financeia_backend.dto.analisis.AnalisisResponse;
import com.financeia.financeia_backend.service.AnalisisFinancieroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analisis-financiero")
public class AnalisisController {

    private final AnalisisFinancieroService analisisFinancieroService;

    public AnalisisController(
            AnalisisFinancieroService analisisFinancieroService
    ) {
        this.analisisFinancieroService = analisisFinancieroService;
    }

    @PostMapping
    public ResponseEntity<AnalisisResponse> analizar(
            @Valid @RequestBody AnalisisRequest request
    ) {
        AnalisisResponse response =
                analisisFinancieroService.analizar(request);

        return ResponseEntity.ok(response);
    }
}