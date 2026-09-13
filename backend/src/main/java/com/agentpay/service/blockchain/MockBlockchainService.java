package com.agentpay.service.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deterministic local simulation of AgentBudget.sol.
 * Enforces the exact same non-negotiable budget checks and idempotency rules as the Solidity contract.
 */
@Service
public class MockBlockchainService implements BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(MockBlockchainService.class);

    private static class MockAgentState {
        String agentId;
        long budgetPaise;
        long spentPaise;
        boolean active;

        MockAgentState(String agentId, long budgetPaise) {
            this.agentId = agentId;
            this.budgetPaise = budgetPaise;
            this.spentPaise = 0L;
            this.active = true;
        }
    }

    private static class MockPaymentRecord {
        String txHash;
        String agentId;
        String requestId;
        long amountPaise;
        String providerId;
        long timestamp;

        MockPaymentRecord(String txHash, String agentId, String requestId, long amountPaise, String providerId) {
            this.txHash = txHash;
            this.agentId = agentId;
            this.requestId = requestId;
            this.amountPaise = amountPaise;
            this.providerId = providerId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final Map<String, MockAgentState> agents = new ConcurrentHashMap<>();
    private final Map<String, MockPaymentRecord> processedRequests = new ConcurrentHashMap<>();

    @Override
    public String getMode() {
        return "MOCK";
    }

    @Override
    public String getEnforcementLayer() {
        return "DETERMINISTIC CONTRACT SIMULATION";
    }

    @Override
    public synchronized void registerAgent(String agentId, Long budgetPaise) {
        log.info("[MOCK CONTRACT] Registering agent '{}' with budget {} paise (₹{})", agentId, budgetPaise, budgetPaise / 100.0);
        agents.put(agentId, new MockAgentState(agentId, budgetPaise));
    }

    @Override
    public Long getBudget(String agentId) {
        MockAgentState state = agents.get(agentId);
        return state != null ? state.budgetPaise : 0L;
    }

    @Override
    public Long getSpent(String agentId) {
        MockAgentState state = agents.get(agentId);
        return state != null ? state.spentPaise : 0L;
    }

    @Override
    public Long getRemaining(String agentId) {
        MockAgentState state = agents.get(agentId);
        if (state == null) return 0L;
        return Math.max(0L, state.budgetPaise - state.spentPaise);
    }

    @Override
    public boolean isRequestProcessed(String agentId, String requestId) {
        String key = agentId + ":" + requestId;
        return processedRequests.containsKey(key);
    }

    @Override
    public synchronized BlockchainPaymentResult processPayment(String agentId, String requestId, Long amountPaise, String providerId) {
        String key = agentId + ":" + requestId;

        // Idempotency check: if already processed, return existing result without charging again
        if (processedRequests.containsKey(key)) {
            MockPaymentRecord existing = processedRequests.get(key);
            MockAgentState state = agents.get(agentId);
            long rem = state != null ? (state.budgetPaise - state.spentPaise) : 0L;
            long sp = state != null ? state.spentPaise : 0L;
            log.info("[MOCK CONTRACT] Idempotent retry detected for key '{}'. Returning existing txHash: {}", key, existing.txHash);
            return BlockchainPaymentResult.approved(existing.txHash, getMode(), getEnforcementLayer(), sp, rem, true);
        }

        MockAgentState state = agents.get(agentId);
        if (state == null || !state.active) {
            log.warn("[MOCK CONTRACT] Rejected: Agent '{}' does not exist or is inactive", agentId);
            return BlockchainPaymentResult.rejected("AGENT_NOT_ACTIVE", getMode(), getEnforcementLayer(), 0L, 0L);
        }

        long remaining = Math.max(0L, state.budgetPaise - state.spentPaise);

        // AUTHORITATIVE SMART CONTRACT CHECK: amount must not exceed remaining budget
        if (amountPaise > remaining) {
            log.warn("[MOCK CONTRACT] SPENDING_LIMIT_EXCEEDED: Requested {} paise (₹{}) > Remaining {} paise (₹{}). Payment BLOCKED!",
                    amountPaise, amountPaise / 100.0, remaining, remaining / 100.0);
            return BlockchainPaymentResult.rejected("SPENDING_LIMIT_EXCEEDED", getMode(), getEnforcementLayer(), state.spentPaise, remaining);
        }

        // Authoritative mutation
        state.spentPaise += amountPaise;
        long newRemaining = state.budgetPaise - state.spentPaise;
        String txHash = generateDeterministicMockHash(agentId, requestId, amountPaise);

        MockPaymentRecord record = new MockPaymentRecord(txHash, agentId, requestId, amountPaise, providerId);
        processedRequests.put(key, record);

        log.info("[MOCK CONTRACT] Payment APPROVED. TxHash: {}. Amount: {} paise (₹{}). New Spent: {} paise (₹{}). New Remaining: {} paise (₹{})",
                txHash, amountPaise, amountPaise / 100.0, state.spentPaise, state.spentPaise / 100.0, newRemaining, newRemaining / 100.0);

        return BlockchainPaymentResult.approved(txHash, getMode(), getEnforcementLayer(), state.spentPaise, newRemaining, false);
    }

    @Override
    public synchronized void resetDemoState() {
        log.info("[MOCK CONTRACT] Resetting demo state to pristine condition.");
        processedRequests.clear();
        for (MockAgentState state : agents.values()) {
            state.spentPaise = 0L;
        }
    }

    private String generateDeterministicMockHash(String agentId, String requestId, Long amountPaise) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = "agentpay_mock:" + agentId + ":" + requestId + ":" + amountPaise + ":" + System.currentTimeMillis();
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder("0xmock_");
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 42); // standard address/tx length
        } catch (NoSuchAlgorithmException e) {
            return "0xmock_" + System.currentTimeMillis();
        }
    }
}
