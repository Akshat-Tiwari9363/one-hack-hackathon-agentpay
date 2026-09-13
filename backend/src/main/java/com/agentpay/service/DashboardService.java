package com.agentpay.service;

import com.agentpay.domain.Agent;
import com.agentpay.dto.DashboardSummaryDto;
import com.agentpay.repository.AgentRepository;
import com.agentpay.repository.ProviderRepository;
import com.agentpay.repository.PurchaseRepository;
import com.agentpay.service.ai.OllamaService;
import com.agentpay.service.blockchain.BlockchainService;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final AgentRepository agentRepository;
    private final ProviderRepository providerRepository;
    private final PurchaseRepository purchaseRepository;
    private final AuditService auditService;
    private final BlockchainService blockchainService;
    private final OllamaService ollamaService;

    public DashboardService(
            AgentRepository agentRepository,
            ProviderRepository providerRepository,
            PurchaseRepository purchaseRepository,
            AuditService auditService,
            BlockchainService blockchainService,
            OllamaService ollamaService) {
        this.agentRepository = agentRepository;
        this.providerRepository = providerRepository;
        this.purchaseRepository = purchaseRepository;
        this.auditService = auditService;
        this.blockchainService = blockchainService;
        this.ollamaService = ollamaService;
    }

    public DashboardSummaryDto getSummary() {
        Agent agent = agentRepository.findByExternalId("agent-demo-001")
                .orElse(new Agent("agent-demo-001", "Demo Agent", "0x7a23c4d8719f9b329a4310e5f29c8b91", 100000L, "ACTIVE"));

        // Query authoritative blockchain state
        Long budgetPaise = blockchainService.getBudget(agent.getExternalId());
        if (budgetPaise == null || budgetPaise == 0) {
            budgetPaise = agent.getBudgetPaise();
        }
        Long spentPaise = blockchainService.getSpent(agent.getExternalId());
        Long remainingPaise = blockchainService.getRemaining(agent.getExternalId());

        long successfulTxns = purchaseRepository.countByPaymentStatus("APPROVED");
        long blockedAttacks = auditService.countBlockedAttacks();
        long totalPurchases = purchaseRepository.count();
        long providersCount = providerRepository.count();

        double utilization = (budgetPaise != null && budgetPaise > 0) 
                ? Math.min(100.0, (spentPaise * 100.0) / budgetPaise) 
                : 0.0;

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setBudgetPaise(budgetPaise);
        summary.setBudgetINR(budgetPaise != null ? budgetPaise / 100.0 : 1000.00);
        summary.setSpentPaise(spentPaise);
        summary.setSpentINR(spentPaise != null ? spentPaise / 100.0 : 0.00);
        summary.setRemainingPaise(remainingPaise);
        summary.setRemainingINR(remainingPaise != null ? remainingPaise / 100.0 : 1000.00);
        summary.setUtilizationPercent(utilization);
        summary.setSuccessfulTxnsCount(successfulTxns);
        summary.setBlockedAttacksCount(blockedAttacks);
        summary.setTotalPurchasesCount(totalPurchases);
        summary.setProvidersCount(providersCount);

        summary.setAgentName(agent.getName());
        summary.setAgentStatus(agent.getStatus());
        summary.setAgentWallet(agent.getWalletAddress());

        summary.setBlockchainMode(blockchainService.getMode());
        summary.setEnforcementLayer(blockchainService.getEnforcementLayer());

        summary.setAiEngine(ollamaService.getEngineName());
        summary.setAiModel(ollamaService.getModelName());

        summary.setCurrency("INR");
        summary.setDailyCapINR(summary.getBudgetINR());
        summary.setBudgetEnforced(true);

        return summary;
    }
}
