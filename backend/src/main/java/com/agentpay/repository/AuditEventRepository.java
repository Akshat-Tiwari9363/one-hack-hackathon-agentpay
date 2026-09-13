package com.agentpay.repository;

import com.agentpay.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByRequestIdOrderByCreatedAtAsc(String requestId);
    List<AuditEvent> findAllByOrderByCreatedAtDesc();
    long countByEventType(String eventType);
}
