package com.agentpay;

import com.agentpay.domain.DeliveryReceipt;
import com.agentpay.dto.PaymentRequestDto;
import com.agentpay.dto.PaymentResponseDto;
import com.agentpay.dto.VerifyReceiptResponseDto;
import com.agentpay.repository.PurchaseRepository;
import com.agentpay.service.PaymentService;
import com.agentpay.service.ReceiptService;
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
public class Sha256ReceiptVerificationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReceiptService receiptService;

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
    @DisplayName("Original delivered content verifies successfully; tampered content triggers tamper detection FAILED")
    void testSha256RecalculationAndTamperDetection() {
        PaymentRequestDto req = new PaymentRequestDto("REQ-VERIFY-001", "agent-demo-001", "prov-a", "svc-trans-a");
        PaymentResponseDto res = paymentService.processPayment(req);

        String receiptId = res.getReceiptId();
        assertThat(receiptId).isNotBlank();

        DeliveryReceipt receipt = receiptService.getByReceiptId(receiptId);
        String expectedHash = ReceiptService.calculateSha256(receipt.getDeliveredContent());
        assertThat(receipt.getContentHash()).isEqualTo(expectedHash);

        // 1. Verify unmodified original content
        VerifyReceiptResponseDto verifyOk = receiptService.verifyReceipt(receiptId, null);
        assertThat(verifyOk.isHashMatches()).isTrue();
        assertThat(verifyOk.getStatus()).isEqualTo("VERIFIED");

        // 2. Verify adversarial tampered content
        VerifyReceiptResponseDto verifyTampered = receiptService.verifyReceipt(receiptId, "TAMPERED_INJECTED_CONTENT");
        assertThat(verifyTampered.isHashMatches()).isFalse();
        assertThat(verifyTampered.getStatus()).isEqualTo("FAILED");
        assertThat(verifyTampered.getMessage()).contains("Tamper detection triggered");
    }
}
