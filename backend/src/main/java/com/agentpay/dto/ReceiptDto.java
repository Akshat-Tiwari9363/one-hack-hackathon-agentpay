package com.agentpay.dto;

import java.time.Instant;

public class ReceiptDto {
    private Long id;
    private String receiptId;
    private Long purchaseId;
    private String requestId;
    private String agentId;
    private String providerId;
    private String serviceId;
    private Long amountPaise;
    private Double amountINR;
    private String currency;
    private String transactionHash;
    private String deliveredContent;
    private String contentHash;
    private String verificationStatus;
    private Instant deliveredAt;

    public ReceiptDto() {}

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
    public void setAmountPaise(Long amountPaise) {
        this.amountPaise = amountPaise;
        this.amountINR = amountPaise != null ? amountPaise / 100.0 : 0.0;
    }

    public Double getAmountINR() { return amountINR; }
    public void setAmountINR(Double amountINR) { this.amountINR = amountINR; }

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
