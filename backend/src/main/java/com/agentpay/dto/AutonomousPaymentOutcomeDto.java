package com.agentpay.dto;

/**
 * Captures the detailed outcome of an individual payment attempt within an autonomous multi-service run.
 * All user-facing amounts are denominated in TOKENS (where 1 TOKEN = 100 internal contract units / paise).
 */
public class AutonomousPaymentOutcomeDto {

    private int paymentNumber;
    private String requestId;
    private String serviceId;
    private String serviceName;
    private String providerId;
    private Long amountTokens;
    private Long amountPaise;
    private String blockchainStatus; // "ALLOWED" or "DENIED"
    private String transactionHash;
    private String serviceStatus;    // "DELIVERED" or "NOT_DELIVERED"
    private String deliveredContent;
    private String contentHash;
    private String receiptId;
    private String reason;
    private String enforcementLayer;

    public AutonomousPaymentOutcomeDto() {}

    public AutonomousPaymentOutcomeDto(
            int paymentNumber,
            String requestId,
            String serviceId,
            String serviceName,
            String providerId,
            Long amountTokens,
            Long amountPaise,
            String blockchainStatus,
            String transactionHash,
            String serviceStatus,
            String deliveredContent,
            String contentHash,
            String receiptId,
            String reason,
            String enforcementLayer) {
        this.paymentNumber = paymentNumber;
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.providerId = providerId;
        this.amountTokens = amountTokens;
        this.amountPaise = amountPaise;
        this.blockchainStatus = blockchainStatus;
        this.transactionHash = transactionHash;
        this.serviceStatus = serviceStatus;
        this.deliveredContent = deliveredContent;
        this.contentHash = contentHash;
        this.receiptId = receiptId;
        this.reason = reason;
        this.enforcementLayer = enforcementLayer;
    }

    public int getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(int paymentNumber) { this.paymentNumber = paymentNumber; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public Long getAmountTokens() { return amountTokens; }
    public void setAmountTokens(Long amountTokens) { this.amountTokens = amountTokens; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getBlockchainStatus() { return blockchainStatus; }
    public void setBlockchainStatus(String blockchainStatus) { this.blockchainStatus = blockchainStatus; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }

    public String getDeliveredContent() { return deliveredContent; }
    public void setDeliveredContent(String deliveredContent) { this.deliveredContent = deliveredContent; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getEnforcementLayer() { return enforcementLayer; }
    public void setEnforcementLayer(String enforcementLayer) { this.enforcementLayer = enforcementLayer; }
}
