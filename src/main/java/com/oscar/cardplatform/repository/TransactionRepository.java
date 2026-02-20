package com.oscar.cardplatform.repository;

import com.oscar.cardplatform.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
