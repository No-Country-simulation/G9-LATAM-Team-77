package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.transaction.TransactionRequest;
import com.financeia.financeia_backend.dto.transaction.TransactionResponse;
import com.financeia.financeia_backend.entity.Transaction;
import com.financeia.financeia_backend.entity.TransactionType;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionResponse create(
            TransactionRequest request,
            User user
    ) {

        Transaction transaction = new Transaction();

        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        transaction.setCategory(request.category());

        String rawType = request.type() != null ? request.type().trim().toUpperCase() : "GASTO";
        TransactionType type;
        if (rawType.equals("INCOME") || rawType.equals("INGRESO")) {
            type = TransactionType.INGRESO;
        } else {
            type = TransactionType.GASTO;
        }
        transaction.setType(type);

        transaction.setDate(request.date());
        transaction.setUser(user);

        Transaction saved = transactionRepository.save(transaction);

        return toResponse(saved);
    }

    public List<TransactionResponse> findByUser(User user) {

        return transactionRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionResponse toResponse(Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getType().name(),
                transaction.getDate()
        );
    }
}