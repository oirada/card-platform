package com.oscar.cardplatform.service;

import com.oscar.cardplatform.domain.entity.AuditEvent;
import com.oscar.cardplatform.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class AuditServiceTest {

    @Mock
    AuditEventRepository auditEventRepository;

    AuditService auditService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditService = new AuditService(auditEventRepository);
    }

    @Test
    void logCardCreation_savesAuditEvent() {
        auditService.logCardCreation("id-123", "4111111111111111", "Test User");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).saveAndFlush(captor.capture());

        AuditEvent ev = captor.getValue();
        assertThat(ev.getEntityType()).isEqualTo("Card");
        assertThat(ev.getOperationType()).isEqualTo("CREATE");
        assertThat(ev.getEntityId()).isEqualTo("id-123");
        assertThat(ev.getDescription()).contains("Tarjeta creada");
    }
}

