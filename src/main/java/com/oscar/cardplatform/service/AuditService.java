package com.oscar.cardplatform.service;

import com.oscar.cardplatform.domain.entity.AuditEvent;
import com.oscar.cardplatform.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public void logCardCreation(String identificador, String pan, String titular) {
        auditEventRepository.saveAndFlush(AuditEvent.builder()
                .entityType("Card")
                .operationType("CREATE")
                .entityId(identificador)
                .oldValues(null)
                .newValues(String.format("{\"pan_hash\":\"%s\", \"titular\":\"%s\", \"status\":\"CREADA\"}",
                        hashForDisplay(pan), titular))
                .performedBy("SYSTEM")
                .timestamp(LocalDateTime.now())
                .description("Tarjeta creada exitosamente")
                .build());
    }

    public void logCardEnrolment(String identificador) {
        auditEventRepository.saveAndFlush(AuditEvent.builder()
                .entityType("Card")
                .operationType("UPDATE")
                .entityId(identificador)
                .oldValues("{\"status\":\"CREADA\"}")
                .newValues("{\"status\":\"ENROLADA\"}")
                .performedBy("SYSTEM")
                .timestamp(LocalDateTime.now())
                .description("Tarjeta enrolada exitosamente")
                .build());
    }

    public void logCardDeletion(String identificador) {
        auditEventRepository.saveAndFlush(AuditEvent.builder()
                .entityType("Card")
                .operationType("DELETE")
                .entityId(identificador)
                .oldValues(null)
                .newValues("{\"status\":\"INACTIVE\"}")
                .performedBy("SYSTEM")
                .timestamp(LocalDateTime.now())
                .description("Tarjeta marcada como inactiva (borrado lógico)")
                .build());
    }

    public void logTransactionCreation(String transactionReference, String cardIdentificador, Double amount) {
        auditEventRepository.saveAndFlush(AuditEvent.builder()
                .entityType("Transaction")
                .operationType("CREATE")
                .entityId(transactionReference)
                .oldValues(null)
                .newValues(String.format("{\"card_id\":\"%s\", \"amount\":%f, \"status\":\"APPROVED\"}",
                        cardIdentificador, amount))
                .performedBy("SYSTEM")
                .timestamp(LocalDateTime.now())
                .description("Transacción creada exitosamente")
                .build());
    }

    public void logTransactionAnnulment(String transactionReference, String cardIdentificador) {
        auditEventRepository.saveAndFlush(AuditEvent.builder()
                .entityType("Transaction")
                .operationType("UPDATE")
                .entityId(transactionReference)
                .oldValues("{\"status\":\"APPROVED\"}")
                .newValues("{\"status\":\"REJECTED\"}")
                .performedBy("SYSTEM")
                .timestamp(LocalDateTime.now())
                .description("Transacción anulada exitosamente")
                .build());
    }

    private String hashForDisplay(String pan) {
        return pan.substring(0, Math.min(6, pan.length()));
    }
}
