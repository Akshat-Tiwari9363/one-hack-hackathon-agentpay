package com.agentpay.service.ai;

import com.agentpay.controller.ServiceResourceController;
import com.agentpay.domain.ServiceEntity;
import com.agentpay.dto.*;
import com.agentpay.exception.SpendingLimitExceededException;
import com.agentpay.repository.ServiceRepository;
import com.agentpay.service.AuditService;
import com.agentpay.service.PaymentService;
import com.agentpay.service.blockchain.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the full autonomous multi-service purchasing loop for AgentPay.
 * Triggered by ONE START CLICK from the user.
 *
 * Coordinates:
 * 1. Service discovery from existing catalog
 * 2. DeepSeek-R1 evaluation and next-action selection
 * 3. Real HTTP 402 Payment Required challenge invocation
 * 4. Automatic payment submission through existing PaymentService
 * 5. Authoritative smart contract spending enforcement (Solidity on Sepolia)
 * 6. Service delivery upon ALLOW / Safe continuation upon DENY
 * 7. Verification of receipts and full multi-payment timeline generation in TOKENS.
 */
@Service
public class AutonomousAgentService {

    private static final Logger log = LoggerFactory.getLogger(AutonomousAgentService.class);
    private static final String DEFAULT_AGENT_ID = "agent-demo-001";
    private static final String DEFAULT_TASK = "Analyze multi-provider translation options, select optimal services, and procure autonomous deliverables within budget.";

    private final ServiceRepository serviceRepository;
    private final ServiceResourceController serviceResourceController;
    private final PaymentService paymentService;
    private final BlockchainService blockchainService;
    private final OllamaService ollamaService;
    private final AuditService auditService;
    private final int maxActions;

    public AutonomousAgentService(
            ServiceRepository serviceRepository,
            ServiceResourceController serviceResourceController,
            PaymentService paymentService,
            BlockchainService blockchainService,
            OllamaService ollamaService,
            AuditService auditService,
            @Value("${app.autonomous-agent.max-actions:5}") int maxActions) {
        this.serviceRepository = serviceRepository;
        this.serviceResourceController = serviceResourceController;
        this.paymentService = paymentService;
        this.blockchainService = blockchainService;
        this.ollamaService = ollamaService;
        this.auditService = auditService;
        this.maxActions = maxActions;
    }

    /**
     * Executes the complete autonomous multi-service purchasing run.
     */
    public AutonomousRunResponseDto startAutonomousRun(AutonomousPurchaseRequestDto request) {
        String agentId = (request != null && request.getAgentId() != null && !request.getAgentId().isBlank())
                ? request.getAgentId()
                : DEFAULT_AGENT_ID;

        String userTask = (request != null && request.getPrompt() != null && !request.getPrompt().isBlank())
                ? request.getPrompt()
                : DEFAULT_TASK;

        boolean adversarial = request != null && request.isSimulateAdversarialOverspend();
        int actionLimit = (request != null && request.getMaxActions() != null && request.getMaxActions() > 0)
                ? request.getMaxActions()
                : this.maxActions;

        String runId = "RUN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("[AUTONOMOUS AGENT] Starting autonomous multi-service run '{}' for agent '{}'. Task: '{}'",
                runId, agentId, userTask);

        auditService.recordEvent(
                runId,
                agentId,
                "AUTONOMOUS_RUN_STARTED",
                "INFO",
                "Autonomous run started by single user trigger. AI Model: " + ollamaService.getModelName()
                        + ", Engine: " + ollamaService.getEngineName(),
                null,
                null
        );

        // 1. Service Discovery from trusted active catalog
        List<ServiceEntity> candidates = serviceRepository.findAll().stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .toList();

        auditService.recordEvent(
                runId,
                agentId,
                "AI_SERVICES_DISCOVERED",
                "INFO",
                "Autonomous agent discovered " + candidates.size() + " active candidate services across registered providers.",
                null,
                null
        );

        List<AutonomousPaymentOutcomeDto> paymentOutcomes = new ArrayList<>();
        int paymentCount = 0;
        String finalSummary = "Autonomous purchasing run completed successfully.";

        // Multi-Step Autonomous Agent Loop
        for (int step = 1; step <= actionLimit; step++) {
            Long remainingPaise = blockchainService.getRemaining(agentId);
            long remainingTokens = remainingPaise != null ? remainingPaise / 100 : 0L;

            log.info("[AUTONOMOUS AGENT] Step {}: Querying DeepSeek-R1. Remaining budget: {} TOKENS.", step, remainingTokens);

            // 2. Ask DeepSeek-R1 to evaluate candidates, history, and decide next action
            OllamaService.AIActionDecision decision = ollamaService.decideNextAction(
                    userTask,
                    candidates,
                    remainingTokens,
                    paymentOutcomes,
                    adversarial
            );

            log.info("[AUTONOMOUS AGENT] Step {}: DeepSeek decision -> Action: '{}', ServiceId: '{}', Reason: '{}'",
                    step, decision.getAction(), decision.getServiceId(), decision.getReason());

            // Check if agent decides it has completed its objective
            if (decision.isDone()) {
                log.info("[AUTONOMOUS AGENT] DeepSeek concluded autonomous task at step {}: {}", step, decision.getReason());
                finalSummary = decision.getReason();
                break;
            }

            // Validate chosen service against database catalog
            String selectedServiceId = decision.getServiceId();
            ServiceEntity serviceEntity = candidates.stream()
                    .filter(s -> s.getExternalId().equalsIgnoreCase(selectedServiceId))
                    .findFirst()
                    .orElse(null);

            if (serviceEntity == null) {
                log.warn("[AUTONOMOUS AGENT] DeepSeek selected unknown service '{}'. Concluding run.", selectedServiceId);
                break;
            }

            paymentCount++;
            String reqId = (request != null && request.getCustomRequestId() != null && step == 1)
                    ? request.getCustomRequestId()
                    : "REQ-AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            auditService.recordEvent(
                    reqId,
                    agentId,
                    "AI_SERVICE_SELECTED",
                    "INFO",
                    "DeepSeek autonomously selected '" + serviceEntity.getName() + "' (" + serviceEntity.getExternalId()
                            + ") from Provider " + serviceEntity.getProviderId() + " for " + (serviceEntity.getPricePaise() / 100)
                            + " TOKENS. Rationale: " + decision.getReason(),
                    serviceEntity.getPricePaise(),
                    null
            );

            // 3. Request Protected Resource to trigger real HTTP 402 Challenge
            try {
                serviceResourceController.requestProtectedResource(
                        serviceEntity.getExternalId(),
                        agentId,
                        reqId,
                        null
                );
            } catch (Exception e) {
                log.debug("[AUTONOMOUS AGENT] HTTP 402 challenge triggered for service: {}", serviceEntity.getExternalId());
            }

            // 4. Automatic Payment Submission through existing PaymentService
            PaymentRequestDto payRequest = new PaymentRequestDto(
                    reqId,
                    agentId,
                    serviceEntity.getProviderId(),
                    serviceEntity.getExternalId()
            );

            try {
                PaymentResponseDto payResponse = paymentService.processPayment(payRequest);

                // Blockchain ALLOWED & Service Delivered
                AutonomousPaymentOutcomeDto outcome = new AutonomousPaymentOutcomeDto(
                        paymentCount,
                        reqId,
                        serviceEntity.getExternalId(),
                        serviceEntity.getName(),
                        serviceEntity.getProviderId(),
                        payResponse.getAmountPaise() / 100, // in TOKENS
                        payResponse.getAmountPaise(),
                        "ALLOWED",
                        payResponse.getTransactionHash(),
                        "DELIVERED",
                        payResponse.getDeliveredContent(),
                        payResponse.getContentHash(),
                        payResponse.getReceiptId(),
                        decision.getReason(),
                        payResponse.getEnforcementLayer()
                );

                paymentOutcomes.add(outcome);

                log.info("[AUTONOMOUS AGENT] Payment #{} ALLOWED on blockchain. Tx: {}, Service delivered.",
                        paymentCount, payResponse.getTransactionHash());

            } catch (SpendingLimitExceededException ex) {
                // Blockchain DENIED (Hard spending limit boundary enforced by Solidity)
                AutonomousPaymentOutcomeDto outcome = new AutonomousPaymentOutcomeDto(
                        paymentCount,
                        reqId,
                        serviceEntity.getExternalId(),
                        serviceEntity.getName(),
                        serviceEntity.getProviderId(),
                        ex.getRequestedAmountPaise() / 100, // in TOKENS
                        ex.getRequestedAmountPaise(),
                        "DENIED",
                        null,
                        "NOT_DELIVERED",
                        null,
                        null,
                        null,
                        "SPENDING_LIMIT_EXCEEDED: Rejected by " + ex.getEnforcementLayer()
                                + ". Required " + (ex.getRequestedAmountPaise() / 100) + " TOKENS, but remaining is "
                                + (ex.getRemainingAmountPaise() / 100) + " TOKENS.",
                        ex.getEnforcementLayer()
                );

                paymentOutcomes.add(outcome);

                log.warn("[AUTONOMOUS AGENT] Payment #{} DENIED by blockchain ({}). Required: {} TOKENS, Remaining: {} TOKENS. Service NOT delivered.",
                        paymentCount, ex.getEnforcementLayer(), ex.getRequestedAmountPaise() / 100, ex.getRemainingAmountPaise() / 100);

                // Re-enable adversarial flag off so agent attempts safe remaining service if possible
                adversarial = false;

            } catch (Exception ex) {
                log.error("[AUTONOMOUS AGENT] Payment #{} failed with unexpected error: {}", paymentCount, ex.getMessage());
                AutonomousPaymentOutcomeDto outcome = new AutonomousPaymentOutcomeDto(
                        paymentCount,
                        reqId,
                        serviceEntity.getExternalId(),
                        serviceEntity.getName(),
                        serviceEntity.getProviderId(),
                        serviceEntity.getPricePaise() / 100,
                        serviceEntity.getPricePaise(),
                        "DENIED",
                        null,
                        "NOT_DELIVERED",
                        null,
                        null,
                        null,
                        "ERROR: " + ex.getMessage(),
                        blockchainService.getEnforcementLayer()
                );
                paymentOutcomes.add(outcome);
            }
        }

        // Build Final Autonomous Run Summary
        int approvedCount = (int) paymentOutcomes.stream().filter(p -> "ALLOWED".equalsIgnoreCase(p.getBlockchainStatus())).count();
        int deniedCount = (int) paymentOutcomes.stream().filter(p -> "DENIED".equalsIgnoreCase(p.getBlockchainStatus())).count();

        Long onChainBudget = blockchainService.getBudget(agentId);
        Long onChainSpent = blockchainService.getSpent(agentId);
        Long onChainRemaining = blockchainService.getRemaining(agentId);

        long budgetTokens = onChainBudget != null ? onChainBudget / 100 : 1000L;
        long spentTokens = onChainSpent != null ? onChainSpent / 100 : 0L;
        long remainingTokens = onChainRemaining != null ? onChainRemaining / 100 : 1000L;

        String runStatus = (deniedCount == 0 && approvedCount > 0) ? "COMPLETED" : (approvedCount > 0 ? "PARTIAL" : "COMPLETED");

        auditService.recordEvent(
                runId,
                agentId,
                "AUTONOMOUS_RUN_COMPLETED",
                "SUCCESS",
                String.format("Autonomous multi-service run finished. Attempted: %d, Approved: %d, Denied: %d. Total Spent: %d TOKENS, Remaining: %d TOKENS.",
                        paymentOutcomes.size(), approvedCount, deniedCount, spentTokens, remainingTokens),
                onChainSpent,
                null
        );

        return new AutonomousRunResponseDto(
                runId,
                agentId,
                userTask,
                runStatus,
                budgetTokens,
                spentTokens,
                remainingTokens,
                paymentOutcomes.size(),
                approvedCount,
                deniedCount,
                paymentOutcomes,
                ollamaService.getEngineName(),
                ollamaService.getModelName(),
                finalSummary
        );
    }
}
