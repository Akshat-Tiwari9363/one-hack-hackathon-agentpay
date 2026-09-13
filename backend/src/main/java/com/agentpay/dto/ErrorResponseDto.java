package com.agentpay.dto;

import java.time.Instant;

public class ErrorResponseDto {
    private String timestamp;
    private String requestId;
    private int status;
    private String code;
    private String message;
    private String currency;
    private Long requestedAmountPaise;
    private Double requestedAmountINR;
    private Long remainingAmountPaise;
    private Double remainingAmountINR;
    private String enforcementLayer;

    public ErrorResponseDto() {
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponseDto(String requestId, int status, String code, String message, Long requestedAmountPaise, Long remainingAmountPaise, String enforcementLayer) {
        this.timestamp = Instant.now().toString();
        this.requestId = requestId;
        this.status = status;
        this.code = code;
        this.message = message;
        this.currency = "INR";
        this.requestedAmountPaise = requestedAmountPaise;
        this.requestedAmountINR = requestedAmountPaise != null ? requestedAmountPaise / 100.0 : null;
        this.remainingAmountPaise = remainingAmountPaise;
        this.remainingAmountINR = remainingAmountPaise != null ? remainingAmountPaise / 100.0 : null;
        this.enforcementLayer = enforcementLayer;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getRequestedAmountPaise() { return requestedAmountPaise; }
    public void setRequestedAmountPaise(Long requestedAmountPaise) { this.requestedAmountPaise = requestedAmountPaise; }

    public Double getRequestedAmountINR() { return requestedAmountINR; }
    public void setRequestedAmountINR(Double requestedAmountINR) { this.requestedAmountINR = requestedAmountINR; }

    public Long getRemainingAmountPaise() { return remainingAmountPaise; }
    public void setRemainingAmountPaise(Long remainingAmountPaise) { this.remainingAmountPaise = remainingAmountPaise; }

    public Double getRemainingAmountINR() { return remainingAmountINR; }
    public void setRemainingAmountINR(Double remainingAmountINR) { this.remainingAmountINR = remainingAmountINR; }

    public String getEnforcementLayer() { return enforcementLayer; }
    public void setEnforcementLayer(String enforcementLayer) { this.enforcementLayer = enforcementLayer; }
}
