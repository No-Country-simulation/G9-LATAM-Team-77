package com.financeia.financeia_backend.controllers;

import tools.jackson.databind.JsonNode;
import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import com.financeia.financeia_backend.service.DataScienceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analisis-financiero")
public class AnalisisController {

    private final DataScienceService dataScienceService;

    public AnalisisController(DataScienceService dataScienceService) {
        this.dataScienceService = dataScienceService;
    }

    @PostMapping
    public ResponseEntity<JsonNode> analizar(
            @Valid @RequestBody AnalisisDataScienceRequest request
    ) {

        JsonNode response = dataScienceService.analizar(request);

        return ResponseEntity.ok(response);
    }
}