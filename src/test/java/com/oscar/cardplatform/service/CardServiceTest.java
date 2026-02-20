package com.oscar.cardplatform.service;

import com.oscar.cardplatform.domain.entity.Card;
import com.oscar.cardplatform.repository.AuditLogRepository;
import com.oscar.cardplatform.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CardServiceTest {

    @Test
    void shouldCreateCard() {
        CardRepository cardRepository = Mockito.mock(CardRepository.class);
        AuditLogRepository auditLogRepository = Mockito.mock(AuditLogRepository.class);

        CardService service = new CardService(cardRepository, auditLogRepository);

        Mockito.when(cardRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Card card = service.createCard("4111111111111111");

        assertThat(card.getMaskedPan()).startsWith("4111");
        assertThat(card.getStatus().name()).isEqualTo("ACTIVE");
    }
}
