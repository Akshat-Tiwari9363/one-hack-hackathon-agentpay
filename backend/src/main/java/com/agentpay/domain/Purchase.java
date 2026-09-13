package com.agentpay.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "purchases", uniqueConstraints = {
    @UniqueConstraint(name = "uk_purchase_request_id", columnNames = {"request_id"})
})
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "agent_id", nullable = false, length = 64)
    private String agentId;

    @Column(name = "provider_id", nullable = false, length = 64)
    private String providerId;

    @Column(name = "service_id", nullable = false, length = 64)
    private String serviceId;

    @Column(name = "amount_paise", nullable = false)
    private Long amountPaise;

    @Column(nullable = false, length = 8)
    private String currency; // "INR"

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, RETRY_RETURNED

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // APPROVED, REJECTED

    @Column(name = "transaction_hash", length = 128)
    private String transactionHash;

    @Column(name = "blockchain_mode", nullable = false, length = 16)
    private String blockchainMode; // MOCK, SEPOLIA

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Purchase() {}

    public Purchase(String requestId, String agentId, String providerId, String serviceId, Long amountPaise, String currency, String status, String paymentStatus, String transactionHash, String blockchainMode) {
        this.requestId = requestId;
        this.agentId = agentId;
        this.providerId = providerId;
        this.serviceId = serviceId;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.transactionHash = transactionHash;
        this.blockchainMode = blockchainMode;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (currency == null) currency = "INR";
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public String getBlockchainMode() { return blockchainMode; }
    public void setBlockchainMode(String blockchainMode) { this.blockchainMode = blockchainMode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
