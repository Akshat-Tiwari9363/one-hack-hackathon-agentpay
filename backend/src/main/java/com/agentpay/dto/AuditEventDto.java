package com.agentpay.dto;

import java.time.Instant;

public class AuditEventDto {
    private Long id;
    private String requestId;
    private String agentId;
    private String eventType;
    private String status;
    private String message;
    private Long amountPaise;
    private Double amountINR;
    private String currency;
    private String transactionHash;
    private Instant createdAt;

    public AuditEventDto() {}

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
    public void setAmountPaise(Long amountPaise) {
        this.amountPaise = amountPaise;
        this.amountINR = amountPaise != null ? amountPaise / 100.0 : null;
    }

    public Double getAmountINR() { return amountINR; }
    public void setAmountINR(Double amountINR) { this.amountINR = amountINR; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
