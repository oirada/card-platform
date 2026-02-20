package com.oscar.cardplatform.service;

import com.oscar.cardplatform.domain.entity.*;
import com.oscar.cardplatform.repository.AuditLogRepository;
import com.oscar.cardplatform.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    public Transaction registerTransaction(Card card, BigDecimal amount, String description) {

        Transaction tx = Transaction.builder()
                .card(card)
                .amount(amount)
                .description(description)
                .status(TransactionStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(tx);

        auditLogRepository.save(AuditLog.builder()
                .action("REGISTER_TX")
                .resource("Transaction")
                .detail("Tx for card " + card.getMaskedPan() + " amount " + amount)
                .createdAt(LocalDateTime.now())
                .build());

        return saved;
    }
}
