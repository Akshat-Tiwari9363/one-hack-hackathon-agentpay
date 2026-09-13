package com.agentpay.dto;

import java.time.Instant;

public class PurchaseDto {
    private Long id;
    private String requestId;
    private String agentId;
    private String providerId;
    private String serviceId;
    private Long amountPaise;
    private Double amountINR;
    private String currency;
    private String status;
    private String paymentStatus;
    private String transactionHash;
    private String blockchainMode;
    private Instant createdAt;

    public PurchaseDto() {}

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
    public void setAmountPaise(Long amountPaise) {
        this.amountPaise = amountPaise;
        this.amountINR = amountPaise != null ? amountPaise / 100.0 : 0.0;
    }

    public Double getAmountINR() { return amountINR; }
    public void setAmountINR(Double amountINR) { this.amountINR = amountINR; }

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
}
