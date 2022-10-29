package com.example.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.domain.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long>{
	
	Optional<Transaction> findByTransactionId(String transactionId);
}
