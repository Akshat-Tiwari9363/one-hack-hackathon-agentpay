package com.agentpay.service;

import com.agentpay.domain.AuditEvent;
import com.agentpay.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public AuditEvent recordEvent(String requestId, String agentId, String eventType, String status, String message, Long amountPaise, String transactionHash) {
        AuditEvent event = new AuditEvent(requestId, agentId, eventType, status, message, amountPaise, "INR", transactionHash);
        AuditEvent saved = auditEventRepository.save(event);
        log.info("[AUDIT TRAIL] [Req: {}] [Event: {}] [Status: {}] [Amount: ₹{}] -> {}",
                requestId, eventType, status, amountPaise != null ? amountPaise / 100.0 : 0.0, message);
        return saved;
    }

    public List<AuditEvent> getTimelineByRequestId(String requestId) {
        return auditEventRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
    }

    public List<AuditEvent> getAllEvents() {
        return auditEventRepository.findAllByOrderByCreatedAtDesc();
    }

    public long countBlockedAttacks() {
        return auditEventRepository.countByEventType("OVERSPEND_BLOCKED");
    }
}
