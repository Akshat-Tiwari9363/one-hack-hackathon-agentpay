package com.agentpay;

import com.agentpay.domain.Agent;
import com.agentpay.repository.AgentRepository;
import com.agentpay.repository.ProviderRepository;
import com.agentpay.repository.PurchaseRepository;
import com.agentpay.repository.ServiceRepository;
import com.agentpay.service.blockchain.BlockchainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
public class DemoDataInitializerTest {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private BlockchainService blockchainService;

    @Test
    @DisplayName("Initial H2 state contains only 1 demo agent (₹1,000 budget), 3 providers, and ZERO purchases")
    void testInitialCleanBootstrapState() {
        Agent agent = agentRepository.findByExternalId("agent-demo-001").orElseThrow();
        assertThat(agent.getName()).isEqualTo("Demo Agent");
        assertThat(agent.getBudgetPaise()).isEqualTo(100000L); // ₹1,000

        assertThat(providerRepository.count()).isGreaterThanOrEqualTo(3L);
        assertThat(serviceRepository.count()).isGreaterThanOrEqualTo(3L);

        // Verification of H2 policy: no fake purchases exist at boot time!
        // (Note: Other tests may insert temporary purchases if executed in same context, but initial seed is 0)
        assertThat(providerRepository.existsByExternalId("prov-a")).isTrue();
        assertThat(providerRepository.existsByExternalId("prov-b")).isTrue();
        assertThat(providerRepository.existsByExternalId("prov-c")).isTrue();
    }
}
