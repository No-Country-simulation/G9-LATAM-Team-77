package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.dashboard.DashboardResponse;
import com.financeia.financeia_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getSummary() {
        // Mocked authenticated user ID for MVP
        Long usuarioId = 1L; 
        DashboardResponse response = dashboardService.getLatestSummary(usuarioId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<DashboardResponse>> getHistory() {
        // Mocked authenticated user ID for MVP
        Long usuarioId = 1L;
        return ResponseEntity.ok(dashboardService.getHistory(usuarioId));
    }
}
