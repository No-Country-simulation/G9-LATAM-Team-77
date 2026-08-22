package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.transaction.TransactionRequest;
import com.financeia.financeia_backend.dto.transaction.TransactionResponse;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.create(request, user));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findByUser(
            @AuthenticationPrincipal User user
    ) {

        return ResponseEntity.ok(
                transactionService.findByUser(user)
        );
    }
}