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

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
public class ConcurrentRetryTest {

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
    @DisplayName("Concurrent requests with the exact same requestId result in exactly 1 charge and 1 purchase")
    void testConcurrentDuplicateRequests() throws Exception {
        String requestId = "REQ-CONCURRENT-RACE";
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Simultaneous release
                    PaymentRequestDto req = new PaymentRequestDto(requestId, "agent-demo-001", "prov-a", "svc-trans-a");
                    PaymentResponseDto resp = paymentService.processPayment(req);
                    if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Handled
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Fire both threads simultaneously
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly one charge settled, spent is ₹300, 1 purchase in database
        assertThat(purchaseRepository.count()).isEqualTo(1L);
        assertThat(blockchainService.getSpent("agent-demo-001")).isEqualTo(30000L);
    }
}
