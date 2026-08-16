package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.transaction.TransactionRequest;
import com.financeia.financeia_backend.dto.transaction.TransactionResponse;
import com.financeia.financeia_backend.entity.Transaction;
import com.financeia.financeia_backend.entity.TransactionType;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;


    @Test
    void deberiaCrearTransaccionCorrectamente() {

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

        Transaction savedTransaction = new Transaction();

        savedTransaction.setId(10L);
        savedTransaction.setDescription("Compra supermercado");
        savedTransaction.setAmount(new BigDecimal("25000.00"));
        savedTransaction.setCategory("Alimentación");
        savedTransaction.setType(TransactionType.GASTO);
        savedTransaction.setDate(LocalDate.of(2026, 8, 14));
        savedTransaction.setUser(user);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        TransactionResponse response =
                transactionService.create(request, user);

        assertNotNull(response);

        assertEquals(10L, response.id());
        assertEquals("Compra supermercado", response.description());
        assertEquals(
                new BigDecimal("25000.00"),
                response.amount()
        );
        assertEquals("Alimentación", response.category());
        assertEquals("GASTO", response.type());
        assertEquals(
                LocalDate.of(2026, 8, 14),
                response.date()
        );

        verify(transactionRepository).save(any(Transaction.class));
    }


    @Test
    void deberiaAsociarLaTransaccionAlUsuarioCorrecto() {

        User user = new User();
        user.setId(1L);

        TransactionRequest request = new TransactionRequest(
                "Compra supermercado",
                new BigDecimal("25000.00"),
                "Alimentación",
                "GASTO",
                LocalDate.of(2026, 8, 14)
        );

        Transaction savedTransaction = new Transaction();

        savedTransaction.setId(10L);
        savedTransaction.setDescription("Compra supermercado");
        savedTransaction.setAmount(new BigDecimal("25000.00"));
        savedTransaction.setCategory("Alimentación");
        savedTransaction.setType(TransactionType.GASTO);
        savedTransaction.setDate(LocalDate.of(2026, 8, 14));
        savedTransaction.setUser(user);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        transactionService.create(request, user);

        verify(transactionRepository).save(
                argThat(transaction ->
                        transaction.getUser() == user
                )
        );
    }


    @Test
    void deberiaObtenerSoloLasTransaccionesDelUsuario() {

        User user = new User();
        user.setId(1L);
        user.setName("Juan");

        Transaction transaction1 = new Transaction();
        transaction1.setId(10L);
        transaction1.setDescription("Supermercado");
        transaction1.setAmount(new BigDecimal("25000.00"));
        transaction1.setCategory("Alimentación");
        transaction1.setType(TransactionType.GASTO);
        transaction1.setDate(LocalDate.of(2026, 8, 14));
        transaction1.setUser(user);

        Transaction transaction2 = new Transaction();
        transaction2.setId(11L);
        transaction2.setDescription("Salario");
        transaction2.setAmount(new BigDecimal("500000.00"));
        transaction2.setCategory("Trabajo");
        transaction2.setType(TransactionType.INGRESO);
        transaction2.setDate(LocalDate.of(2026, 8, 14));
        transaction2.setUser(user);

        when(transactionRepository.findByUser(user))
                .thenReturn(List.of(transaction1, transaction2));

        List<TransactionResponse> response =
                transactionService.findByUser(user);

        assertNotNull(response);

        assertEquals(2, response.size());

        assertEquals(
                "Supermercado",
                response.get(0).description()
        );

        assertEquals(
                "Salario",
                response.get(1).description()
        );

        verify(transactionRepository)
                .findByUser(user);
    }


    @Test
    void deberiaDevolverListaVaciaSiElUsuarioNoTieneTransacciones() {

        User user = new User();
        user.setId(1L);

        when(transactionRepository.findByUser(user))
                .thenReturn(List.of());

        List<TransactionResponse> response =
                transactionService.findByUser(user);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(transactionRepository)
                .findByUser(user);
    }
}