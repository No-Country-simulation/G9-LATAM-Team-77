package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.transaction.TransactionRequest;
import com.financeia.financeia_backend.dto.transaction.TransactionResponse;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transactionController =
                new TransactionController(transactionService);
    }


    @Test
    void deberiaCrearTransaccion() {

        User user = new User();
        user.setId(1L);
        user.setName("Juan");

        TransactionRequest request = new TransactionRequest(
                "Compra supermercado",
                new BigDecimal("25000.00"),
                "Alimentación",
                "GASTO",
                LocalDate.of(2026, 8, 14)
        );

        TransactionResponse response = new TransactionResponse(
                10L,
                "Compra supermercado",
                new BigDecimal("25000.00"),
                "Alimentación",
                "GASTO",
                LocalDate.of(2026, 8, 14)
        );

        when(transactionService.create(request, user))
                .thenReturn(response);

        ResponseEntity<TransactionResponse> result =
                transactionController.create(request, user);

        assertEquals(
                HttpStatus.CREATED,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());

        assertEquals(
                10L,
                result.getBody().id()
        );

        assertEquals(
                "Compra supermercado",
                result.getBody().description()
        );

        assertEquals(
                new BigDecimal("25000.00"),
                result.getBody().amount()
        );

        assertEquals(
                "Alimentación",
                result.getBody().category()
        );

        assertEquals(
                "GASTO",
                result.getBody().type()
        );

        assertEquals(
                LocalDate.of(2026, 8, 14),
                result.getBody().date()
        );
    }


    @Test
    void deberiaEnviarElUsuarioCorrectoAlServicio() {

        User user = new User();
        user.setId(1L);

        TransactionRequest request = new TransactionRequest(
                "Compra supermercado",
                new BigDecimal("25000.00"),
                "Alimentación",
                "GASTO",
                LocalDate.of(2026, 8, 14)
        );

        TransactionResponse response = new TransactionResponse(
                10L,
                "Compra supermercado",
                new BigDecimal("25000.00"),
                "Alimentación",
                "GASTO",
                LocalDate.of(2026, 8, 14)
        );

        when(transactionService.create(request, user))
                .thenReturn(response);

        transactionController.create(request, user);

        verify(transactionService)
                .create(request, user);
    }


    @Test
    void deberiaObtenerTransaccionesDelUsuario() {

        User user = new User();
        user.setId(1L);
        user.setName("Juan");

        TransactionResponse transaction1 = new TransactionResponse(
                10L,
                "Compra supermercado",
                new BigDecimal("25000.00"),
                "Alimentación",
                "GASTO",
                LocalDate.of(2026, 8, 14)
        );

        TransactionResponse transaction2 = new TransactionResponse(
                11L,
                "Salario",
                new BigDecimal("500000.00"),
                "Trabajo",
                "INGRESO",
                LocalDate.of(2026, 8, 14)
        );

        when(transactionService.findByUser(user))
                .thenReturn(List.of(transaction1, transaction2));

        ResponseEntity<List<TransactionResponse>> result =
                transactionController.findByUser(user);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());

        assertEquals(
                2,
                result.getBody().size()
        );

        assertEquals(
                "Compra supermercado",
                result.getBody().get(0).description()
        );

        assertEquals(
                "Salario",
                result.getBody().get(1).description()
        );
    }


    @Test
    void deberiaEnviarElUsuarioCorrectoAlBuscarTransacciones() {

        User user = new User();
        user.setId(1L);

        when(transactionService.findByUser(user))
                .thenReturn(List.of());

        transactionController.findByUser(user);

        verify(transactionService)
                .findByUser(user);
    }
}