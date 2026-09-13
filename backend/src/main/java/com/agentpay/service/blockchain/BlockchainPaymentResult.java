package com.agentpay.service.blockchain;

public class BlockchainPaymentResult {
    private final boolean success;
    private final String transactionHash;
    private final String rejectionReason;
    private final String mode;
    private final String enforcementLayer;
    private final Long spentPaise;
    private final Long remainingPaise;
    private final boolean isRetry;

    private BlockchainPaymentResult(boolean success, String transactionHash, String rejectionReason, String mode, String enforcementLayer, Long spentPaise, Long remainingPaise, boolean isRetry) {
        this.success = success;
        this.transactionHash = transactionHash;
        this.rejectionReason = rejectionReason;
        this.mode = mode;
        this.enforcementLayer = enforcementLayer;
        this.spentPaise = spentPaise;
        this.remainingPaise = remainingPaise;
        this.isRetry = isRetry;
    }

    public static BlockchainPaymentResult approved(String transactionHash, String mode, String enforcementLayer, Long spentPaise, Long remainingPaise, boolean isRetry) {
        return new BlockchainPaymentResult(true, transactionHash, null, mode, enforcementLayer, spentPaise, remainingPaise, isRetry);
    }

    public static BlockchainPaymentResult rejected(String rejectionReason, String mode, String enforcementLayer, Long spentPaise, Long remainingPaise) {
        return new BlockchainPaymentResult(false, null, rejectionReason, mode, enforcementLayer, spentPaise, remainingPaise, false);
    }

    public boolean isSuccess() { return success; }
    public String getTransactionHash() { return transactionHash; }
    public String getRejectionReason() { return rejectionReason; }
    public String getMode() { return mode; }
    public String getEnforcementLayer() { return enforcementLayer; }
    public Long getSpentPaise() { return spentPaise; }
    public Long getRemainingPaise() { return remainingPaise; }
    public boolean isRetry() { return isRetry; }
}
