package com.agentpay.init;

import com.agentpay.domain.Agent;
import com.agentpay.domain.Provider;
import com.agentpay.domain.ServiceEntity;
import com.agentpay.repository.AgentRepository;
import com.agentpay.repository.ProviderRepository;
import com.agentpay.repository.ServiceRepository;
import com.agentpay.service.blockchain.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final AgentRepository agentRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;
    private final BlockchainService blockchainService;

    public DemoDataInitializer(
            AgentRepository agentRepository,
            ProviderRepository providerRepository,
            ServiceRepository serviceRepository,
            BlockchainService blockchainService) {
        this.agentRepository = agentRepository;
        this.providerRepository = providerRepository;
        this.serviceRepository = serviceRepository;
        this.blockchainService = blockchainService;
    }

    @Override
    public void run(String... args) {
        log.info("[BOOTSTRAP] Initializing deterministic demo data in H2 persistence layer...");

        // 1. Initialize Demo Agent: ₹1,000 budget (100,000 paise)
        String agentId = "agent-demo-001";
        if (!agentRepository.existsByExternalId(agentId)) {
            Agent agent = new Agent(
                    agentId,
                    "Demo Agent",
                    "0x7a23c4d8719f9b329a4310e5f29c8b91",
                    100000L, // ₹1,000 = 100,000 paise
                    "ACTIVE"
            );
            agentRepository.save(agent);
            log.info("[BOOTSTRAP] Created Agent: {} with authorized budget: ₹1,000 (100,000 paise)", agent.getName());
        }

        // Register agent on blockchain layer (enforcing ₹1,000 cap)
        blockchainService.registerAgent(agentId, 100000L);

        // 2. Initialize Provider A: ₹300 (30,000 paise), quality 90
        if (!providerRepository.existsByExternalId("prov-a")) {
            Provider provA = new Provider(
                    "prov-a",
                    "Provider A (LinguaFast)",
                    "High-speed neural translation services optimized for autonomous micro-transactions.",
                    90,
                    "ACTIVE"
            );
            providerRepository.save(provA);

            ServiceEntity svcA = new ServiceEntity(
                    "svc-trans-a",
                    "prov-a",
                    "Fast Neural Translation",
                    "TRANSLATION",
                    "Standard latency translation pipeline (up to 1,000 tokens) with 90% SLA compliance.",
                    30000L, // ₹300
                    "INR",
                    90,
                    "ACTIVE"
            );
            serviceRepository.save(svcA);
            log.info("[BOOTSTRAP] Seeded Provider A with service '{}' priced at ₹300 (30,000 paise)", svcA.getName());
        }

        // 3. Initialize Provider B: ₹500 (50,000 paise), quality 97
        if (!providerRepository.existsByExternalId("prov-b")) {
            Provider provB = new Provider(
                    "prov-b",
                    "Provider B (PolyGlot Ultra)",
                    "Enterprise certified contextual translation with guaranteed high fidelity.",
                    97,
                    "ACTIVE"
            );
            providerRepository.save(provB);

            ServiceEntity svcB = new ServiceEntity(
                    "svc-trans-b",
                    "prov-b",
                    "Ultra Precision Translation",
                    "TRANSLATION",
                    "Context-preserving neural translation with 97% SLA and guaranteed zero hallucination.",
                    50000L, // ₹500
                    "INR",
                    97,
                    "ACTIVE"
            );
            serviceRepository.save(svcB);
            log.info("[BOOTSTRAP] Seeded Provider B with service '{}' priced at ₹500 (50,000 paise)", svcB.getName());
        }

        // 4. Initialize Provider C: ₹200 (20,000 paise), quality 82
        if (!providerRepository.existsByExternalId("prov-c")) {
            Provider provC = new Provider(
                    "prov-c",
                    "Provider C (BudgetLingua)",
                    "Economical translation tier for high-volume background tasks.",
                    82,
                    "ACTIVE"
            );
            providerRepository.save(provC);

            ServiceEntity svcC = new ServiceEntity(
                    "svc-trans-c",
                    "prov-c",
                    "Economy Translation",
                    "TRANSLATION",
                    "Best-effort translation service for low-criticality autonomous workflows.",
                    20000L, // ₹200
                    "INR",
                    82,
                    "ACTIVE"
            );
            serviceRepository.save(svcC);
            log.info("[BOOTSTRAP] Seeded Provider C with service '{}' priced at ₹200 (20,000 paise)", svcC.getName());
        }

        // 5. Initialize Overspend Test Service: ₹800 (80,000 paise), quality 99
        // This is used for the key judging demonstration where remaining is ₹700 and agent requests ₹800
        if (!serviceRepository.existsByExternalId("svc-trans-overspend")) {
            ServiceEntity svcOverspend = new ServiceEntity(
                    "svc-trans-overspend",
                    "prov-b",
                    "Enterprise Translation Suite",
                    "TRANSLATION",
                    "High-end enterprise language compute cluster priced at ₹800 (used to test overspend bounds).",
                    80000L, // ₹800
                    "INR",
                    99,
                    "ACTIVE"
            );
            serviceRepository.save(svcOverspend);
            log.info("[BOOTSTRAP] Seeded Overspend Test Service '{}' priced at ₹800 (80,000 paise)", svcOverspend.getName());
        }

        log.info("[BOOTSTRAP] Bootstrap complete! Initial State: Budget=₹1,000, Spent=₹0, Remaining=₹1,000, Purchases=0, Blocked=0");
    }
}
