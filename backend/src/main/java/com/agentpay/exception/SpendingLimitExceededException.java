package com.agentpay.exception;

public class SpendingLimitExceededException extends RuntimeException {

    private final String requestId;
    private final Long requestedAmountPaise;
    private final Long remainingAmountPaise;
    private final String enforcementLayer;

    public SpendingLimitExceededException(String message, String requestId, Long requestedAmountPaise, Long remainingAmountPaise, String enforcementLayer) {
        super(message);
        this.requestId = requestId;
        this.requestedAmountPaise = requestedAmountPaise;
        this.remainingAmountPaise = remainingAmountPaise;
        this.enforcementLayer = enforcementLayer;
    }

    public String getRequestId() { return requestId; }
    public Long getRequestedAmountPaise() { return requestedAmountPaise; }
    public Long getRemainingAmountPaise() { return remainingAmountPaise; }
    public String getEnforcementLayer() { return enforcementLayer; }
}
