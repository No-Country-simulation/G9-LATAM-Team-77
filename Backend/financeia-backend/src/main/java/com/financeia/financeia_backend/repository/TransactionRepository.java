package com.financeia.financeia_backend.repository;

import com.financeia.financeia_backend.entity.Transaction;
import com.financeia.financeia_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);

}







