package com.oscar.cardplatform.web.controller;

import com.oscar.cardplatform.domain.entity.Transaction;
import com.oscar.cardplatform.service.TransactionService;
import com.oscar.cardplatform.service.CardService;
import com.oscar.cardplatform.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CardService cardService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @PostMapping
    public ResponseEntity<CreateTransactionResponse> create(@Valid @RequestBody CreateTransactionRequest req) {
        try {
            Transaction tx = transactionService.registerTransactionByIdentificador(req.identificador(), req.referencia(), req.total(), req.direccion());
            CreateTransactionResponse resp = new CreateTransactionResponse("00", "Compra exitosa", tx.getStatus().name(), tx.getReference());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException iae) {
            logger.warn("Transaction create failed (hide detail): {}", iae.getMessage());
            // Para casos de "Tarjeta no existe" o referencia inválida devolvemos 404/400 según el mensaje
            String msg = iae.getMessage() != null ? iae.getMessage().toLowerCase() : "";
            if (msg.contains("tarjeta no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CreateTransactionResponse("01", "Operación inválida", null, null));
            }
            if (msg.contains("referencia duplicada") || msg.contains("duplicad")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new CreateTransactionResponse("03", "Referencia duplicada", null, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateTransactionResponse("01", "Operación inválida", null, null));
        } catch (IllegalStateException ise) {
            logger.warn("Transaction create invalid state: {}", ise.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new CreateTransactionResponse("02", "Operación inválida", null, null));
        } catch (Exception e) {
            logger.error("Unexpected error creating transaction", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateTransactionResponse("01", "Operación inválida", null, null));
        }
    }

    @PostMapping("/annul")
    public ResponseEntity<AnnulTransactionResponse> annul(@Valid @RequestBody AnnulTransactionRequest req) {
        try {
            Transaction tx = transactionService.annulTransaction(req.identificador(), req.referencia(), req.total());
            return ResponseEntity.ok(new AnnulTransactionResponse("00", "Compra anulada", tx.getReference()));
        } catch (IllegalArgumentException iae) {
            logger.warn("Annul attempt failed (hide detail): {}", iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AnnulTransactionResponse("01", "Operación inválida", null));
        } catch (IllegalStateException ise) {
            logger.warn("Annul cannot be performed (older than 5min): {}", ise.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new AnnulTransactionResponse("02", "Operación inválida", null));
        } catch (Exception e) {
            logger.error("Unexpected error annulling transaction", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AnnulTransactionResponse("01", "Operación inválida", null));
        }
    }
}
