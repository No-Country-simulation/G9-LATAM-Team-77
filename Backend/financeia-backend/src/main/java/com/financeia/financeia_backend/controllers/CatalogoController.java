package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.catalogo.MonedaResponse;
import com.financeia.financeia_backend.dto.catalogo.PaisResponse;
import com.financeia.financeia_backend.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/paises")
    public ResponseEntity<List<PaisResponse>> findPaises() {
        return ResponseEntity.ok(catalogoService.findPaises());
    }

    @GetMapping("/monedas")
    public ResponseEntity<List<MonedaResponse>> findMonedas() {
        return ResponseEntity.ok(catalogoService.findMonedas());
    }
}
