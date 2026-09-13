package com.agentpay.dto;

public class VerifyReceiptResponseDto {
    private String receiptId;
    private String calculatedContentHash;
    private String storedContentHash;
    private boolean hashMatches;
    private String status; // VERIFIED, FAILED
    private String message;

    public VerifyReceiptResponseDto() {}

    public VerifyReceiptResponseDto(String receiptId, String calculatedContentHash, String storedContentHash, boolean hashMatches, String status, String message) {
        this.receiptId = receiptId;
        this.calculatedContentHash = calculatedContentHash;
        this.storedContentHash = storedContentHash;
        this.hashMatches = hashMatches;
        this.status = status;
        this.message = message;
    }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getCalculatedContentHash() { return calculatedContentHash; }
    public void setCalculatedContentHash(String calculatedContentHash) { this.calculatedContentHash = calculatedContentHash; }

    public String getStoredContentHash() { return storedContentHash; }
    public void setStoredContentHash(String storedContentHash) { this.storedContentHash = storedContentHash; }

    public boolean isHashMatches() { return hashMatches; }
    public void setHashMatches(boolean hashMatches) { this.hashMatches = hashMatches; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
