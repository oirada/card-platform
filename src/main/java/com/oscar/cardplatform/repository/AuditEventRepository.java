package com.oscar.cardplatform.repository;

import com.oscar.cardplatform.domain.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByEntityId(String entityId);
    List<AuditEvent> findByEntityType(String entityType);
    List<AuditEvent> findByOperationType(String operationType);
}

