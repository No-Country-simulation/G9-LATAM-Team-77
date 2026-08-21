package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.transaction.TransactionRequest;
import com.financeia.financeia_backend.dto.transaction.TransactionResponse;
import com.financeia.financeia_backend.entity.Transaction;
import com.financeia.financeia_backend.entity.TransactionType;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.exception.ApiException;
import com.financeia.financeia_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

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

        try {
            transaction.setType(TransactionType.valueOf(request.type().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El tipo de transacción no es válido");
        }

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
