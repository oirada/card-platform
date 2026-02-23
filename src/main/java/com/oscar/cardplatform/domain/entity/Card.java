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

    // Nuevo identificador generado a partir del hash del PAN y la fecha
    @Column(nullable = false, unique = true)
    private String identificador;

    // Número de validación generado en la creación (1..100)
    @Column
    private Integer numeroValidacion;

    // Información adicional del titular que vienen en el request
    @Column(nullable = false)
    private String titular;

    @Column(nullable = false)
    private String cedula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType tipo;

    @Column(nullable = false)
    private String telefono;
}
