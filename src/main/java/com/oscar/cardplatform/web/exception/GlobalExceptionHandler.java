// Se añade un handler global para normalizar mensajes expuestos al cliente (genéricos por seguridad)
package com.oscar.cardplatform.web.exception;

import com.oscar.cardplatform.web.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "";

        // Referencia duplicada -> 409 CONFLICT con cuerpo específico de transacción
        if (msg.toLowerCase().contains("referencia duplicada")) {
            logger.warn("Duplicate reference attempted: {}", msg);
            CreateTransactionResponse body = new CreateTransactionResponse("03", "Referencia duplicada", null, null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // Referencia inválida (en anulación) -> 400 BAD_REQUEST con cuerpo genérico para anulación
        if (msg.toLowerCase().contains("referencia") && msg.toLowerCase().contains("inválid")) {
            logger.warn("Invalid reference attempt (security: hiding detail from client): {}", msg);
            AnnulTransactionResponse body = new AnnulTransactionResponse("01", "Operación inválida", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // Recurso no encontrado / tarjeta no existe -> 404 NOT_FOUND con mensaje genérico
        if (msg.toLowerCase().contains("no existe") || msg.toLowerCase().contains("recurso no encontrado") || msg.toLowerCase().contains("identificador")) {
            logger.warn("Resource not found attempt (security: hiding detail from client): {}", msg);
            CreateCardResponse body = new CreateCardResponse("01", "Operación inválida", null, null, null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        // Default: bad request con mensaje genérico
        logger.warn("IllegalArgumentException with message: {}", msg);
        CreateCardResponse defaultBody = new CreateCardResponse("01", "Operación inválida", null, null, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(defaultBody);
    }

    @ExceptionHandler(IllegalStateException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "";
        logger.warn("Invalid state operation (security: hiding detail from client): {}", msg, ex);

        // Anulación no permitida (>5 minutos) -> 409 CONFLICT con código específico
        if (msg.toLowerCase().contains("anul")) {
            AnnulTransactionResponse body = new AnnulTransactionResponse("02", "Operación inválida", null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // Otros estados inválidos -> 409 CONFLICT genérico para transacciones
        CreateTransactionResponse body = new CreateTransactionResponse("02", "Operación inválida", null, null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Violación de integridad (p.ej. unique constraint sobre referencia)
        logger.error("Data integrity violation (likely duplicate reference): {}", ex.getMessage(), ex);
        CreateTransactionResponse body = new CreateTransactionResponse("03", "Referencia duplicada", null, null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOther(Exception ex) {
        logger.error("Unexpected exception (security: hiding detail from client): {}", ex.getMessage(), ex);
        CreateCardResponse defaultBody = new CreateCardResponse("01", "Operación inválida", null, null, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(defaultBody);
    }
}

