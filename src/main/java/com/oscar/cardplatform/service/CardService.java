package com.oscar.cardplatform.service;

import com.oscar.cardplatform.domain.entity.*;
import com.oscar.cardplatform.repository.AuditLogRepository;
import com.oscar.cardplatform.repository.CardRepository;
import com.oscar.cardplatform.service.util.PanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public Card createCard(String pan, String titular, String cedula, String tipo, String telefono) {
        String maskedPan = PanUtils.mask(pan);
        String panHash = PanUtils.hash(pan);
        String identificador = PanUtils.identificador(pan);
        Integer numeroValidacion = generarNumeroValidacion();

        CardType cardType = parseTipo(tipo);

        Card card = Card.builder()
                .maskedPan(maskedPan)
                .panHash(panHash)
                .status(CardStatus.CREADA)
                .createdAt(LocalDateTime.now())
                .identificador(identificador)
                .numeroValidacion(numeroValidacion)
                .titular(titular)
                .cedula(cedula)
                .tipo(cardType)
                .telefono(telefono)
                .build();

        Card saved = cardRepository.save(card);

        auditLogRepository.save(AuditLog.builder()
                .action("CREATE_CARD")
                .resource("Card")
                .detail("Card created: " + maskedPan)
                .createdAt(LocalDateTime.now())
                .build());

        // Registrar evento de auditoría detallado
        try {
            auditService.logCardCreation(saved.getIdentificador(), pan, saved.getTitular());
        } catch (Exception ignore) {
            // No dejar que falle la operación principal
        }

        return saved;
    }

    private CardType parseTipo(String tipo) {
        if (tipo == null) return CardType.DEBITO;
        String n = tipo.trim().toUpperCase();
        if (n.startsWith("C")) return CardType.CREDITO;
        return CardType.DEBITO;
    }

    public Card getCardByPan(String pan) {
        String panHash = PanUtils.hash(pan);
        return cardRepository.findByPanHash(panHash)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));
    }

    public int generarNumeroValidacion() {
        return new Random().nextInt(100) + 1; // 1..100
    }

    public Card getCardByIdentificador(String identificador) {
        return cardRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no existe"));
    }

    public Card enrolCard(String identificador, Integer numeroValidacion) {
        Card card = cardRepository.findByIdentificadorAndNumeroValidacion(identificador, numeroValidacion)
                .orElseThrow(() -> new IllegalArgumentException("Numero de validacion inválido o tarjeta no existe"));

        card = Card.builder()
                .id(card.getId())
                .maskedPan(card.getMaskedPan())
                .panHash(card.getPanHash())
                .status(CardStatus.ENROLADA)
                .createdAt(card.getCreatedAt())
                .identificador(card.getIdentificador())
                .numeroValidacion(card.getNumeroValidacion())
                .titular(card.getTitular())
                .cedula(card.getCedula())
                .tipo(card.getTipo())
                .telefono(card.getTelefono())
                .build();

        Card saved = cardRepository.save(card);

        auditLogRepository.save(AuditLog.builder()
                .action("ENROL_CARD")
                .resource("Card")
                .detail("Card enrolada: " + card.getMaskedPan())
                .createdAt(LocalDateTime.now())
                .build());

        // Registrar evento de auditoría detallado
        try {
            auditService.logCardEnrolment(saved.getIdentificador());
        } catch (Exception ignore) {
        }

        return saved;
    }

    public Card deleteCardLogical(String identificador) {
        Card card = getCardByIdentificador(identificador);
        card = Card.builder()
                .id(card.getId())
                .maskedPan(card.getMaskedPan())
                .panHash(card.getPanHash())
                .status(CardStatus.INACTIVE)
                .createdAt(card.getCreatedAt())
                .identificador(card.getIdentificador())
                .numeroValidacion(card.getNumeroValidacion())
                .titular(card.getTitular())
                .cedula(card.getCedula())
                .tipo(card.getTipo())
                .telefono(card.getTelefono())
                .build();

        Card saved = cardRepository.save(card);

        auditLogRepository.save(AuditLog.builder()
                .action("DELETE_CARD")
                .resource("Card")
                .detail("Card inactivated: " + card.getMaskedPan())
                .createdAt(LocalDateTime.now())
                .build());

        // Registrar evento de auditoría detallado
        try {
            auditService.logCardDeletion(saved.getIdentificador());
        } catch (Exception ignore) {
        }

        return saved;
    }
}
