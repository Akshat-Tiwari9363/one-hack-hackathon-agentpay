package com.agentpay.service.blockchain;

public interface BlockchainService {

    String getMode(); // "MOCK" or "SEPOLIA"

    String getEnforcementLayer(); // "DETERMINISTIC CONTRACT SIMULATION" or "SOLIDITY SMART CONTRACT"

    void registerAgent(String agentId, Long budgetPaise);

    Long getBudget(String agentId);

    Long getSpent(String agentId);

    Long getRemaining(String agentId);

    boolean isRequestProcessed(String agentId, String requestId);

    BlockchainPaymentResult processPayment(String agentId, String requestId, Long amountPaise, String providerId);

    void resetDemoState();
}
