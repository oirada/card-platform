package com.oscar.cardplatform.web.controller;

import com.oscar.cardplatform.domain.entity.Card;
import com.oscar.cardplatform.domain.entity.Transaction;
import com.oscar.cardplatform.service.CardService;
import com.oscar.cardplatform.service.TransactionService;
import com.oscar.cardplatform.web.dto.CreateTransactionRequest;
import com.oscar.cardplatform.web.dto.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestParam String pan,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        Card card = cardService.getCardByPan(pan);
        Transaction tx = transactionService.registerTransaction(card, request.amount(), request.description());
        return ResponseEntity.ok(new TransactionResponse(tx.getId(), tx.getAmount(), tx.getStatus().name()));
    }
}
