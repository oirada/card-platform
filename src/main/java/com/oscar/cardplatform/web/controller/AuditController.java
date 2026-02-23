package com.oscar.cardplatform.web.controller;

import com.oscar.cardplatform.domain.entity.AuditEvent;
import com.oscar.cardplatform.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    @GetMapping("/by-entity/{entityId}")
    public ResponseEntity<List<AuditEvent>> byEntity(@PathVariable String entityId) {
        return ResponseEntity.ok(auditEventRepository.findByEntityId(entityId));
    }

    @GetMapping("/by-type/{entityType}")
    public ResponseEntity<List<AuditEvent>> byType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditEventRepository.findByEntityType(entityType));
    }

    @GetMapping("/by-operation/{operationType}")
    public ResponseEntity<List<AuditEvent>> byOperation(@PathVariable String operationType) {
        return ResponseEntity.ok(auditEventRepository.findByOperationType(operationType));
    }
}

