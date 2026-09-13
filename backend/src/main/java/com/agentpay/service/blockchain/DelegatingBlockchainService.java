package com.agentpay.service.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DelegatingBlockchainService implements BlockchainService {

    private static final Logger log =
            LoggerFactory.getLogger(DelegatingBlockchainService.class);

    private final MockBlockchainService mockBlockchainService;
    private final SepoliaBlockchainService sepoliaBlockchainService;

    private volatile String currentMode;

    public DelegatingBlockchainService(
            MockBlockchainService mockBlockchainService,
            SepoliaBlockchainService sepoliaBlockchainService,
            @Value("${app.blockchain.mode:SEPOLIA}") String defaultMode) {

        this.mockBlockchainService = mockBlockchainService;
        this.sepoliaBlockchainService = sepoliaBlockchainService;

        this.currentMode = normalizeMode(defaultMode);

        log.info(
                "[BLOCKCHAIN LAYER] Initialized with active mode: {}",
                this.currentMode
        );
    }

    private String normalizeMode(String mode) {
        if ("MOCK".equalsIgnoreCase(mode)) {
            return "MOCK";
        }

        return "SEPOLIA";
    }

    public void setMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return;
        }

        String normalized = normalizeMode(mode);

        this.currentMode = normalized;

        log.info(
                "[BLOCKCHAIN LAYER] Switched blockchain mode to: {}",
                this.currentMode
        );
    }

    private BlockchainService active() {
        return "SEPOLIA".equalsIgnoreCase(currentMode)
                ? sepoliaBlockchainService
                : mockBlockchainService;
    }

    @Override
    public String getMode() {
        return active().getMode();
    }

    @Override
    public String getEnforcementLayer() {
        return active().getEnforcementLayer();
    }

    @Override
    public void registerAgent(String agentId, Long budgetPaise) {

        /*
         * Keep the mock state available for tests/demo fallback,
         * but live payment enforcement uses the currently active
         * blockchain mode.
         */
        mockBlockchainService.registerAgent(agentId, budgetPaise);

        if ("SEPOLIA".equalsIgnoreCase(currentMode)) {
            sepoliaBlockchainService.registerAgent(
                    agentId,
                    budgetPaise
            );
        }
    }

    @Override
    public Long getBudget(String agentId) {
        return active().getBudget(agentId);
    }

    @Override
    public Long getSpent(String agentId) {
        return active().getSpent(agentId);
    }

    @Override
    public Long getRemaining(String agentId) {
        return active().getRemaining(agentId);
    }

    @Override
    public boolean isRequestProcessed(
            String agentId,
            String requestId) {

        return active().isRequestProcessed(
                agentId,
                requestId
        );
    }

    @Override
    public BlockchainPaymentResult processPayment(
            String agentId,
            String requestId,
            Long amountPaise,
            String providerId) {

        return active().processPayment(
                agentId,
                requestId,
                amountPaise,
                providerId
        );
    }

    @Override
    public void resetDemoState() {

        /*
         * Always reset the mock state so switching back to MOCK
         * remains deterministic.
         */
        mockBlockchainService.resetDemoState();

        /*
         * When running the actual hackathon configuration,
         * reset the real Sepolia contract as well.
         */
        if ("SEPOLIA".equalsIgnoreCase(currentMode)) {
            sepoliaBlockchainService.resetDemoState();
        }
    }

    public boolean isSepoliaConnected() {
        return sepoliaBlockchainService.isConnected();
    }

    public boolean isSepoliaContractValid() {
        return sepoliaBlockchainService.isValidContract();
    }

    public boolean hasSepoliaBytecode() {
        return sepoliaBlockchainService.hasBytecode();
    }

    public String getSepoliaContractAddress() {
        return sepoliaBlockchainService.getContractAddress();
    }
}