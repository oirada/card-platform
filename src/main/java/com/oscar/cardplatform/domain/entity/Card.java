package com.oscar.cardplatform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * PAN enmascarado: ej 123456****3456
     */
    @Column(nullable = false, unique = true)
    private String maskedPan;

    /**
     * Hash del PAN real
     */
    @Column(nullable = false, unique = true)
    private String panHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
