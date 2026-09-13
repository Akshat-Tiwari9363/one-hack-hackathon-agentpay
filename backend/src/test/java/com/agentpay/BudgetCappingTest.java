package com.agentpay;

import com.agentpay.dto.PaymentRequestDto;
import com.agentpay.dto.PaymentResponseDto;
import com.agentpay.exception.SpendingLimitExceededException;
import com.agentpay.repository.PurchaseRepository;
import com.agentpay.service.AuditService;
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
public class BudgetCappingTest {

    @Autowired
    private PaymentService paymentService;

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
        blockchainService.registerAgent("agent-demo-001", 100000L); // ₹1,000
    }

    @Test
    @DisplayName("Normal purchase of ₹300 succeeds, updating spent to ₹300 and remaining to ₹700")
    void testNormalPurchase() {
        PaymentRequestDto req = new PaymentRequestDto("REQ-TEST-NORM", "agent-demo-001", "prov-a", "svc-trans-a");
        PaymentResponseDto res = paymentService.processPayment(req);

        assertThat(res.getStatus()).isEqualTo("SUCCESS");
        assertThat(res.getPaymentStatus()).isEqualTo("APPROVED");
        assertThat(res.getAmountPaise()).isEqualTo(30000L); // ₹300
        assertThat(res.getAmountINR()).isEqualTo(300.0);
        assertThat(res.getContentHash()).isNotBlank();
        assertThat(res.getDeliveredContent()).contains("नमस्ते दुनिया");

        assertThat(blockchainService.getSpent("agent-demo-001")).isEqualTo(30000L);
        assertThat(blockchainService.getRemaining("agent-demo-001")).isEqualTo(70000L); // ₹700
    }

    @Test
    @DisplayName("CENTRAL SECURITY PROOF: Attempting to spend ₹800 when remaining is ₹700 is BLOCKED by enforcement layer")
    void testOverspendBlockedByEnforcementLayer() {
        // Step 1: Execute normal purchase of ₹300 -> Remaining becomes ₹700
        PaymentRequestDto normReq = new PaymentRequestDto("REQ-TEST-1", "agent-demo-001", "prov-a", "svc-trans-a");
        paymentService.processPayment(normReq);

        assertThat(blockchainService.getRemaining("agent-demo-001")).isEqualTo(70000L); // ₹700 remaining

        // Step 2: Attempt to purchase ₹800 service (svc-trans-overspend)
        PaymentRequestDto overspendReq = new PaymentRequestDto("REQ-TEST-OVER", "agent-demo-001", "prov-b", "svc-trans-overspend");

        assertThatThrownBy(() -> paymentService.processPayment(overspendReq))
                .isInstanceOf(SpendingLimitExceededException.class)
                .hasMessageContaining("SPENDING_LIMIT_EXCEEDED");

        // Step 3: Verify that NO state was mutated and spent remains ₹300
        assertThat(blockchainService.getSpent("agent-demo-001")).isEqualTo(30000L);
        assertThat(blockchainService.getRemaining("agent-demo-001")).isEqualTo(70000L);

        // Step 4: Verify audit trail recorded OVERSPEND_BLOCKED
        assertThat(auditService.countBlockedAttacks()).isGreaterThanOrEqualTo(1);
    }
}
