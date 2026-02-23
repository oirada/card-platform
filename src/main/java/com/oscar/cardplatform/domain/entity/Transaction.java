package com.oscar.cardplatform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String description;

    @Column(unique = true)
    private String reference; // número de referencia (6 dígitos)

    private String address;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne(optional = false)
    private Card card;

    private LocalDateTime createdAt;
}
