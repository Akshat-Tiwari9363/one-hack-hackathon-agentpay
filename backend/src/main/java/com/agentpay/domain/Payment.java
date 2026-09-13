package com.agentpay.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_request_id", columnNames = {"request_id"})
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(name = "agent_id", nullable = false, length = 64)
    private String agentId;

    @Column(name = "amount_paise", nullable = false)
    private Long amountPaise;

    @Column(nullable = false, length = 8)
    private String currency; // "INR"

    @Column(nullable = false)
    private String status; // APPROVED, BLOCKED

    @Column(name = "transaction_hash", length = 128)
    private String transactionHash;

    @Column(name = "blockchain_mode", nullable = false, length = 16)
    private String blockchainMode; // MOCK, SEPOLIA

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Payment() {}

    public Payment(String requestId, Long purchaseId, String agentId, Long amountPaise, String currency, String status, String transactionHash, String blockchainMode) {
        this.requestId = requestId;
        this.purchaseId = purchaseId;
        this.agentId = agentId;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.status = status;
        this.transactionHash = transactionHash;
        this.blockchainMode = blockchainMode;
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

    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public String getBlockchainMode() { return blockchainMode; }
    public void setBlockchainMode(String blockchainMode) { this.blockchainMode = blockchainMode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
