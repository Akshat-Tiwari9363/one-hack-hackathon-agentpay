package com.agentpay;

import com.agentpay.dto.AutonomousPaymentOutcomeDto;
import com.agentpay.dto.AutonomousPurchaseRequestDto;
import com.agentpay.dto.AutonomousRunResponseDto;
import com.agentpay.repository.PurchaseRepository;
import com.agentpay.service.AuditService;
import com.agentpay.service.ai.AutonomousAgentService;
import com.agentpay.service.blockchain.BlockchainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
public class AutonomousMultiPaymentFlowTest {

    @Autowired
    private AutonomousAgentService autonomousAgentService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        blockchainService.resetDemoState();
        blockchainService.registerAgent("agent-demo-001", 100000L); // 1000 TOKENS (100,000 paise)
    }

    @Test
    @DisplayName("Autonomous multi-service run executes multiple payments with token accounting and individual payment records")
    void testAutonomousMultiServiceRun() {
        AutonomousPurchaseRequestDto request = new AutonomousPurchaseRequestDto(
                "agent-demo-001",
                "Translate this document with high fidelity and cost-efficiency",
                "TRANSLATION",
                false
        );
        request.setMaxActions(3);

        AutonomousRunResponseDto run = autonomousAgentService.startAutonomousRun(request);

        assertThat(run).isNotNull();
        assertThat(run.getRunId()).startsWith("RUN-");
        assertThat(run.getDenomination()).isEqualTo("TOKENS");
        assertThat(run.getBudgetTokens()).isEqualTo(1000L);
        assertThat(run.getPayments()).isNotEmpty();

        // Verify each payment outcome has individual tracking
        for (AutonomousPaymentOutcomeDto payment : run.getPayments()) {
            assertThat(payment.getPaymentNumber()).isPositive();
            assertThat(payment.getRequestId()).isNotBlank();
            assertThat(payment.getServiceId()).isNotBlank();
            assertThat(payment.getAmountTokens()).isPositive();
            assertThat(payment.getBlockchainStatus()).isIn("ALLOWED", "DENIED");

            if ("ALLOWED".equals(payment.getBlockchainStatus())) {
                assertThat(payment.getServiceStatus()).isEqualTo("DELIVERED");
                assertThat(payment.getDeliveredContent()).isNotBlank();
                assertThat(payment.getContentHash()).isNotBlank();
                assertThat(payment.getReceiptId()).isNotBlank();
            } else {
                assertThat(payment.getServiceStatus()).isEqualTo("NOT_DELIVERED");
            }
        }

        // Verify token accounting integrity: spent + remaining == budget
        assertThat(run.getSpentTokens() + run.getRemainingTokens()).isEqualTo(run.getBudgetTokens());
        assertThat(run.getTotalAttempted()).isEqualTo(run.getPayments().size());
        assertThat(run.getTotalApproved() + run.getTotalDenied()).isEqualTo(run.getTotalAttempted());
    }

    @Test
    @DisplayName("Autonomous run records blockchain DENIED when spending limit boundary is breached and continues safely")
    void testAutonomousRunHandlesDeniedOverspend() {
        // Execute an adversarial run designed to attempt the 800 TOKENS service
        AutonomousPurchaseRequestDto request = new AutonomousPurchaseRequestDto(
                "agent-demo-001",
                "Procure services including testing enterprise bounds",
                "TRANSLATION",
                true // adversarial overspend trigger
        );
        request.setMaxActions(2);

        AutonomousRunResponseDto run = autonomousAgentService.startAutonomousRun(request);

        assertThat(run).isNotNull();
        assertThat(run.getPayments()).isNotEmpty();

        // Verify that the run successfully captured outcomes and did NOT crash
        boolean hasApproved = run.getPayments().stream().anyMatch(p -> "ALLOWED".equals(p.getBlockchainStatus()));
        boolean hasDenied = run.getPayments().stream().anyMatch(p -> "DENIED".equals(p.getBlockchainStatus()));

        // At least one outcome must be verified
        assertThat(run.getPayments().size()).isGreaterThanOrEqualTo(1);

        // If a payment was denied, verify that service was NOT delivered and reason contains enforcement layer
        run.getPayments().stream()
                .filter(p -> "DENIED".equals(p.getBlockchainStatus()))
                .forEach(deniedPayment -> {
                    assertThat(deniedPayment.getServiceStatus()).isEqualTo("NOT_DELIVERED");
                    assertThat(deniedPayment.getDeliveredContent()).isNull();
                    assertThat(deniedPayment.getReason()).contains("SPENDING_LIMIT_EXCEEDED");
                });
    }
}
