package com.agentpay.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "delivery_receipts")
public class DeliveryReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_id", unique = true, nullable = false, length = 64)
    private String receiptId;

    @Column(name = "purchase_id", nullable = false)
    private Long purchaseId;

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

    @Column(name = "transaction_hash", length = 128)
    private String transactionHash;

    @Column(name = "delivered_content", columnDefinition = "TEXT", nullable = false)
    private String deliveredContent;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash; // SHA-256 of deliveredContent

    @Column(name = "verification_status", nullable = false, length = 32)
    private String verificationStatus; // VERIFIED, TAMPERED, PENDING

    @Column(name = "delivered_at", nullable = false, updatable = false)
    private Instant deliveredAt;

    public DeliveryReceipt() {}

    public DeliveryReceipt(String receiptId, Long purchaseId, String requestId, String agentId, String providerId, String serviceId, Long amountPaise, String currency, String transactionHash, String deliveredContent, String contentHash, String verificationStatus) {
        this.receiptId = receiptId;
        this.purchaseId = purchaseId;
        this.requestId = requestId;
        this.agentId = agentId;
        this.providerId = providerId;
        this.serviceId = serviceId;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.transactionHash = transactionHash;
        this.deliveredContent = deliveredContent;
        this.contentHash = contentHash;
        this.verificationStatus = verificationStatus;
        this.deliveredAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (deliveredAt == null) deliveredAt = Instant.now();
        if (currency == null) currency = "INR";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }

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

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public String getDeliveredContent() { return deliveredContent; }
    public void setDeliveredContent(String deliveredContent) { this.deliveredContent = deliveredContent; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
