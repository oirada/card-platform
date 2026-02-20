package com.oscar.cardplatform.service;

import com.oscar.cardplatform.domain.entity.*;
import com.oscar.cardplatform.repository.AuditLogRepository;
import com.oscar.cardplatform.repository.CardRepository;
import com.oscar.cardplatform.service.util.PanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AuditLogRepository auditLogRepository;

    public Card createCard(String pan) {
        String maskedPan = PanUtils.mask(pan);
        String panHash = PanUtils.hash(pan);

        Card card = Card.builder()
                .maskedPan(maskedPan)
                .panHash(panHash)
                .status(CardStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Card saved = cardRepository.save(card);

        auditLogRepository.save(AuditLog.builder()
                .action("CREATE_CARD")
                .resource("Card")
                .detail("Card created: " + maskedPan)
                .createdAt(LocalDateTime.now())
                .build());

        return saved;
    }

    public Card getCardByPan(String pan) {
        String panHash = PanUtils.hash(pan);
        return cardRepository.findByPanHash(panHash)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));
    }
}
