package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.dashboard.DashboardResponse;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getSummary(
            @AuthenticationPrincipal User user
    ) {

        return ResponseEntity.ok(
                dashboardService.getSummary(user)
        );
    }
}