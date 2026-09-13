package com.agentpay.dto;

public class PaymentResponseDto {
    private String requestId;
    private Long purchaseId;
    private String agentId;
    private String providerId;
    private String serviceId;
    private Long amountPaise;
    private Double amountINR;
    private String currency;
    private String status; // SUCCESS, BLOCKED
    private String paymentStatus; // APPROVED, REJECTED
    private String transactionHash;
    private String blockchainMode;
    private String enforcementLayer;
    private String deliveredContent;
    private String contentHash;
    private String receiptId;
    private boolean isRetry;

    public PaymentResponseDto() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) {
        this.amountPaise = amountPaise;
        this.amountINR = amountPaise != null ? amountPaise / 100.0 : null;
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

    public String getEnforcementLayer() { return enforcementLayer; }
    public void setEnforcementLayer(String enforcementLayer) { this.enforcementLayer = enforcementLayer; }

    public String getDeliveredContent() { return deliveredContent; }
    public void setDeliveredContent(String deliveredContent) { this.deliveredContent = deliveredContent; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public boolean isRetry() { return isRetry; }
    public void setRetry(boolean retry) { isRetry = retry; }
}
