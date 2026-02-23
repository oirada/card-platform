package com.oscar.cardplatform.service;

import com.oscar.cardplatform.domain.entity.*;
import com.oscar.cardplatform.repository.AuditLogRepository;
import com.oscar.cardplatform.repository.TransactionRepository;
import com.oscar.cardplatform.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final CardRepository cardRepository;
    private final AuditService auditService;

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

        try {
            auditService.logTransactionCreation(saved.getReference(), card.getIdentificador(), saved.getAmount().doubleValue());
        } catch (Exception ignore) {
        }

        return saved;
    }

    public Transaction registerTransactionByIdentificador(String identificador, String reference, BigDecimal amount, String address) {
        if (transactionRepository.findByReference(reference).isPresent()) {
            throw new IllegalArgumentException("Referencia duplicada");
        }

        Card card = cardRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no existe"));

        if (card.getStatus() != CardStatus.ENROLADA) {
            throw new IllegalStateException("Tarjeta no enrolada");
        }

        Transaction tx = Transaction.builder()
                .card(card)
                .amount(amount)
                .description("Compra")
                .reference(reference)
                .address(address)
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

        try {
            auditService.logTransactionCreation(saved.getReference(), card.getIdentificador(), saved.getAmount().doubleValue());
        } catch (Exception ignore) {
        }

        return saved;
    }

    public Transaction annulTransaction(String identificador, String reference, BigDecimal amount) {
        Transaction tx = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("numero de referencia inválido"));

        if (!tx.getCard().getIdentificador().equals(identificador)) {
            throw new IllegalArgumentException("numero de referencia inválido");
        }

        Duration diff = Duration.between(tx.getCreatedAt(), LocalDateTime.now());
        if (diff.toMinutes() >= 5) {
            throw new IllegalStateException("No se puede anular transacción");
        }

        tx = Transaction.builder()
                .id(tx.getId())
                .card(tx.getCard())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .reference(tx.getReference())
                .address(tx.getAddress())
                .status(TransactionStatus.REJECTED)
                .createdAt(tx.getCreatedAt())
                .build();

        Transaction saved = transactionRepository.save(tx);

        auditLogRepository.save(AuditLog.builder()
                .action("ANNUL_TX")
                .resource("Transaction")
                .detail("Annul tx ref " + reference)
                .createdAt(LocalDateTime.now())
                .build());

        try {
            auditService.logTransactionAnnulment(saved.getReference(), saved.getCard().getIdentificador());
        } catch (Exception ignore) {
        }

        return saved;
    }
}
