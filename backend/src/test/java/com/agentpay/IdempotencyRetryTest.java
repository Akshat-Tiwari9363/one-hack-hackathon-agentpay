package com.agentpay;

import com.agentpay.dto.PaymentRequestDto;
import com.agentpay.dto.PaymentResponseDto;
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

@SpringBootTest
@ActiveProfiles("dev")
public class IdempotencyRetryTest {

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
        blockchainService.registerAgent("agent-demo-001", 100000L);
    }

    @Test
    @DisplayName("Submitting the exact same requestId twice results in ZERO double charge and reuses receipt")
    void testIdempotentRetryProtection() {
        String requestId = "REQ-IDEMPOTENT-001";
        PaymentRequestDto first = new PaymentRequestDto(requestId, "agent-demo-001", "prov-a", "svc-trans-a");

        // First attempt: settles ₹300
        PaymentResponseDto res1 = paymentService.processPayment(first);
        assertThat(res1.isRetry()).isFalse();
        assertThat(res1.getAmountPaise()).isEqualTo(30000L);
        assertThat(blockchainService.getSpent("agent-demo-001")).isEqualTo(30000L);
        assertThat(purchaseRepository.count()).isEqualTo(1L);

        // Second attempt: same requestId
        PaymentRequestDto second = new PaymentRequestDto(requestId, "agent-demo-001", "prov-a", "svc-trans-a");
        PaymentResponseDto res2 = paymentService.processPayment(second);

        // Verification: Marked as retry, identical txHash/receiptId, zero additional spending
        assertThat(res2.isRetry()).isTrue();
        assertThat(res2.getReceiptId()).isEqualTo(res1.getReceiptId());
        assertThat(res2.getTransactionHash()).isEqualTo(res1.getTransactionHash());
        assertThat(res2.getContentHash()).isEqualTo(res1.getContentHash());

        // Spent MUST still be ₹300, purchases count MUST remain 1
        assertThat(blockchainService.getSpent("agent-demo-001")).isEqualTo(30000L);
        assertThat(blockchainService.getRemaining("agent-demo-001")).isEqualTo(70000L);
        assertThat(purchaseRepository.count()).isEqualTo(1L);
    }
}
