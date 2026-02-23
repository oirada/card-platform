package com.oscar.cardplatform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; // "Card", "Transaction"

    @Column(nullable = false)
    private String operationType; // "CREATE", "UPDATE", "DELETE"

    @Column(nullable = false)
    private String entityId; // identificador de la tarjeta o transacción

    @Column(nullable = true, columnDefinition = "TEXT")
    private String oldValues; // Valores anteriores (JSON o null si CREATE)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String newValues; // Valores nuevos (JSON)

    @Column(nullable = false)
    private String performedBy; // Usuario que realizó la operación (sistema)

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String description; // Descripción adicional del cambio

}
