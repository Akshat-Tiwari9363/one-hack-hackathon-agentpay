package com.agentpay.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "agent_id", nullable = false, length = 64)
    private String agentId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType; // SERVICE_REQUESTED, PAYMENT_REQUIRED, PAYMENT_ATTEMPTED, PAYMENT_APPROVED, PAYMENT_REJECTED, OVERSPEND_BLOCKED, SERVICE_DELIVERED, DELIVERY_VERIFIED, RETRY_DETECTED

    @Column(nullable = false, length = 32)
    private String status; // SUCCESS, BLOCKED, INFO, FAILED

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "amount_paise")
    private Long amountPaise;

    @Column(length = 8)
    private String currency; // "INR"

    @Column(name = "transaction_hash", length = 128)
    private String transactionHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AuditEvent() {}

    public AuditEvent(String requestId, String agentId, String eventType, String status, String message, Long amountPaise, String currency, String transactionHash) {
        this.requestId = requestId;
        this.agentId = agentId;
        this.eventType = eventType;
        this.status = status;
        this.message = message;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.transactionHash = transactionHash;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (currency == null) currency = "INR";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
