package com.agentpay;

import com.agentpay.dto.PaymentRequestDto;
import com.agentpay.exception.SpendingLimitExceededException;
import com.agentpay.repository.PurchaseRepository;
import com.agentpay.service.PaymentService;
import com.agentpay.service.blockchain.BlockchainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("dev")
public class DirectApiAttackTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        blockchainService.resetDemoState();
        blockchainService.registerAgent("agent-demo-001", 100000L); // ₹1,000
    }

    @Test
    @DisplayName("SECURITY PROOF: Direct API call attempting to force unauthorized price override (₹1,500) is BLOCKED")
    void testDirectApiPriceManipulationAttack() {
        // Direct API caller attempts to send an override price of ₹1,500 (150,000 paise > 100,000 paise budget)
        PaymentRequestDto malicious = new PaymentRequestDto(
                "REQ-MALICIOUS-API",
                "agent-demo-001",
                "prov-a",
                "svc-trans-a",
                150000L // ₹1,500 direct attack
        );

        assertThatThrownBy(() -> paymentService.processPayment(malicious))
                .isInstanceOf(SpendingLimitExceededException.class)
                .hasMessageContaining("SPENDING_LIMIT_EXCEEDED");

        assertThat(blockchainService.getSpent("agent-demo-001")).isEqualTo(0L);
        assertThat(blockchainService.getRemaining("agent-demo-001")).isEqualTo(100000L);
    }
}
