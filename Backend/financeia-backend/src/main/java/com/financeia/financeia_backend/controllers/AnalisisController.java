package com.financeia.financeia_backend.controllers;

import tools.jackson.databind.JsonNode;
import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.service.DataScienceService;
import com.financeia.financeia_backend.service.HistorialAnalisisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analisis-financiero")
public class AnalisisController {

    private final DataScienceService dataScienceService;
    private final HistorialAnalisisService historialAnalisisService;

    public AnalisisController(
            DataScienceService dataScienceService,
            HistorialAnalisisService historialAnalisisService
    ) {
        this.dataScienceService = dataScienceService;
        this.historialAnalisisService = historialAnalisisService;
    }

    @PostMapping
    public ResponseEntity<JsonNode> analizar(
            @Valid @RequestBody AnalisisDataScienceRequest request,
            @AuthenticationPrincipal User user
    ) {

        JsonNode response = dataScienceService.analizar(request);
        historialAnalisisService.registrarAnalisisExitoso(user, request, response);

        return ResponseEntity.ok(response);
    }
}
