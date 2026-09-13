package com.agentpay.service;

import com.agentpay.domain.*;
import com.agentpay.dto.PaymentRequestDto;
import com.agentpay.dto.PaymentResponseDto;
import com.agentpay.exception.ResourceNotFoundException;
import com.agentpay.exception.SpendingLimitExceededException;
import com.agentpay.repository.AgentRepository;
import com.agentpay.repository.PaymentRepository;
import com.agentpay.repository.PurchaseRepository;
import com.agentpay.service.blockchain.BlockchainPaymentResult;
import com.agentpay.service.blockchain.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final AgentRepository agentRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final ProviderService providerService;
    private final ReceiptService receiptService;
    private final AuditService auditService;
    private final BlockchainService blockchainService;

    public PaymentService(
            AgentRepository agentRepository,
            PurchaseRepository purchaseRepository,
            PaymentRepository paymentRepository,
            ProviderService providerService,
            ReceiptService receiptService,
            AuditService auditService,
            BlockchainService blockchainService) {
        this.agentRepository = agentRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.providerService = providerService;
        this.receiptService = receiptService;
        this.auditService = auditService;
        this.blockchainService = blockchainService;
    }

    /**
     * Executes the payment workflow with the blockchain/smart contract
     * as the authoritative spending-enforcement layer.
     */
    @Transactional(noRollbackFor = SpendingLimitExceededException.class)
    public synchronized PaymentResponseDto processPayment(PaymentRequestDto request) {

        String requestId = request.getRequestId();
        String agentId = request.getAgentId();
        String providerId = request.getProviderId();
        String serviceId = request.getServiceId();

        log.info(
                "[PAYMENT SERVICE] Received payment request '{}' from agent '{}' for service '{}'",
                requestId,
                agentId,
                serviceId
        );

        /*
         * 1. Database idempotency check.
         *
         * If this request was already successfully settled,
         * return the existing purchase without charging again.
         */
        Optional<Purchase> existingPurchaseOpt =
                purchaseRepository.findByRequestId(requestId);

        if (existingPurchaseOpt.isPresent()) {

            Purchase existing = existingPurchaseOpt.get();

            log.info(
                    "[PAYMENT SERVICE] Idempotent retry detected for requestId: {}. " +
                    "Returning existing purchase without charging again.",
                    requestId
            );

            auditService.recordEvent(
                    requestId,
                    agentId,
                    "RETRY_DETECTED",
                    "INFO",
                    "Idempotent retry intercepted: Returning existing settled purchase. Zero additional charge applied.",
                    existing.getAmountPaise(),
                    existing.getTransactionHash()
            );

            Optional<DeliveryReceipt> receiptOpt =
                    receiptService.findByRequestId(requestId);

            return buildExistingResponse(
                    existing,
                    receiptOpt.orElse(null)
            );
        }

        /*
         * 2. Validate agent.
         */
        Agent agent = agentRepository
                .findByExternalId(agentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agent not found: " + agentId
                        )
                );

        /*
         * 3. Read the authoritative service price from the backend.
         * The client is NEVER allowed to reduce or redefine the authoritative price.
         */
        ServiceEntity service =
                providerService.getServiceEntity(serviceId);

        Long authoritativePricePaise =
                service.getPricePaise();

        /*
         * Client price validation:
         * - Client cannot pay zero or negative amounts.
         * - Client cannot reduce the authoritative service price (e.g. paying ₹1 for ₹300 service).
         * - Any attempt to underpay is rejected with structured error.
         */
        if (request.getOverridePricePaise() != null) {
            if (request.getOverridePricePaise() <= 0) {
                throw new IllegalArgumentException("Payment amount must be greater than zero");
            }
            if (request.getOverridePricePaise() < service.getPricePaise()) {
                log.warn(
                        "[PAYMENT SERVICE] Security violation: Client attempted unauthorized price reduction ({} paise < authoritative {} paise). Rejecting request.",
                        request.getOverridePricePaise(),
                        service.getPricePaise()
                );
                throw new IllegalArgumentException(
                        "PRICE_MANIPULATION_REJECTED: Client cannot reduce authoritative service price of "
                                + serviceId + " (₹" + (service.getPricePaise() / 100.0) + ")"
                );
            }
            if (request.getOverridePricePaise() > service.getPricePaise()) {
                log.warn(
                        "[PAYMENT SERVICE] Direct API override price detected for attack test: {} paise",
                        request.getOverridePricePaise()
                );
                authoritativePricePaise = request.getOverridePricePaise();
            }
        }

        auditService.recordEvent(
                requestId,
                agentId,
                "PAYMENT_ATTEMPTED",
                "INFO",
                "Submitting payment of ₹"
                        + (authoritativePricePaise / 100.0)
                        + " to enforcement layer ("
                        + blockchainService.getEnforcementLayer()
                        + ")",
                authoritativePricePaise,
                null
        );

        /*
         * 4. Authoritative blockchain/smart-contract enforcement.
         */
        BlockchainPaymentResult result =
                blockchainService.processPayment(
                        agentId,
                        requestId,
                        authoritativePricePaise,
                        providerId
                );

        /*
         * Blockchain-level idempotency.
         */
        if (result.isRetry()) {

            log.info(
                    "[PAYMENT SERVICE] Blockchain detected idempotent retry for requestId: {}",
                    requestId
            );

            auditService.recordEvent(
                    requestId,
                    agentId,
                    "RETRY_DETECTED",
                    "INFO",
                    "Idempotent retry intercepted by blockchain: Zero additional charge applied.",
                    authoritativePricePaise,
                    result.getTransactionHash()
            );

            Optional<Purchase> existing =
                    purchaseRepository.findByRequestId(requestId);

            if (existing.isPresent()) {

                Optional<DeliveryReceipt> receiptOpt =
                        receiptService.findByRequestId(requestId);

                return buildExistingResponse(
                        existing.get(),
                        receiptOpt.orElse(null)
                );
            }
        }

        /*
         * 5. Blockchain rejected the payment.
         *
         * Important:
         * No purchase is created.
         * No service is delivered.
         * The rejection is persisted as a blocked payment attempt.
         */
        if (!result.isSuccess()) {

            String reason =
                    result.getRejectionReason();

            log.warn(
                    "[PAYMENT SERVICE] Enforcement Layer REJECTED payment: {}. " +
                    "Requested: ₹{}, Remaining: ₹{}",
                    reason,
                    authoritativePricePaise / 100.0,
                    result.getRemainingPaise() / 100.0
            );

            Payment failedPayment =
                    new Payment(
                            requestId,
                            null,
                            agentId,
                            authoritativePricePaise,
                            "INR",
                            "BLOCKED",
                            null,
                            result.getMode()
                    );

            paymentRepository.save(failedPayment);

            auditService.recordEvent(
                    requestId,
                    agentId,
                    "PAYMENT_REJECTED",
                    "BLOCKED",
                    "Payment rejected by "
                            + result.getEnforcementLayer()
                            + ". Reason: "
                            + reason,
                    authoritativePricePaise,
                    null
            );

            auditService.recordEvent(
                    requestId,
                    agentId,
                    "OVERSPEND_BLOCKED",
                    "BLOCKED",
                    "Enforcement layer prevented unauthorized overspending of ₹"
                            + (authoritativePricePaise / 100.0)
                            + ". Remaining allowed: ₹"
                            + (result.getRemainingPaise() / 100.0)
                            + ". No service delivered.",
                    authoritativePricePaise,
                    null
            );

            throw new SpendingLimitExceededException(
                    "Payment rejected by spending policy: " + reason,
                    requestId,
                    authoritativePricePaise,
                    result.getRemainingPaise(),
                    result.getEnforcementLayer()
            );
        }

        /*
         * 6. Payment approved.
         *
         * Only now do we persist the purchase/payment records.
         */
        try {

            Purchase purchase =
                    new Purchase(
                            requestId,
                            agentId,
                            providerId,
                            serviceId,
                            authoritativePricePaise,
                            "INR",
                            "SUCCESS",
                            "APPROVED",
                            result.getTransactionHash(),
                            result.getMode()
                    );

            Purchase savedPurchase =
                    purchaseRepository.saveAndFlush(purchase);

            Payment payment =
                    new Payment(
                            requestId,
                            savedPurchase.getId(),
                            agentId,
                            authoritativePricePaise,
                            "INR",
                            "APPROVED",
                            result.getTransactionHash(),
                            result.getMode()
                    );

            paymentRepository.save(payment);

            /*
             * Keep the local agent budget synchronized with the
             * authoritative blockchain budget.
             */
            agent.setBudgetPaise(
                    blockchainService.getBudget(agentId)
            );

            agentRepository.save(agent);

            auditService.recordEvent(
                    requestId,
                    agentId,
                    "PAYMENT_APPROVED",
                    "SUCCESS",
                    "Payment of ₹"
                            + (authoritativePricePaise / 100.0)
                            + " authorized on "
                            + result.getEnforcementLayer()
                            + ". Tx: "
                            + result.getTransactionHash(),
                    authoritativePricePaise,
                    result.getTransactionHash()
            );

            /*
             * 7. Deliver the service ONLY after successful payment.
             */
            String deliveredContent =
                    providerService.deliverService(
                            serviceId,
                            providerId,
                            "Hello world"
                    );

            /*
             * 8. Generate cryptographically verifiable receipt.
             */
            DeliveryReceipt receipt =
                    receiptService.createReceipt(
                            savedPurchase.getId(),
                            requestId,
                            agentId,
                            providerId,
                            serviceId,
                            authoritativePricePaise,
                            result.getTransactionHash(),
                            deliveredContent
                    );

            auditService.recordEvent(
                    requestId,
                    agentId,
                    "SERVICE_DELIVERED",
                    "SUCCESS",
                    "Service delivered successfully. Content SHA-256 hash: "
                            + receipt.getContentHash(),
                    authoritativePricePaise,
                    result.getTransactionHash()
            );

            /*
             * 9. Build final successful response.
             */
            PaymentResponseDto response =
                    new PaymentResponseDto();

            response.setRequestId(requestId);
            response.setPurchaseId(savedPurchase.getId());
            response.setAgentId(agentId);
            response.setProviderId(providerId);
            response.setServiceId(serviceId);
            response.setAmountPaise(authoritativePricePaise);
            response.setCurrency("INR");
            response.setStatus("SUCCESS");
            response.setPaymentStatus("APPROVED");
            response.setTransactionHash(result.getTransactionHash());
            response.setBlockchainMode(result.getMode());
            response.setEnforcementLayer(
                    result.getEnforcementLayer()
            );
            response.setDeliveredContent(deliveredContent);
            response.setContentHash(receipt.getContentHash());
            response.setReceiptId(receipt.getReceiptId());
            response.setRetry(false);

            return response;

        } catch (DataIntegrityViolationException e) {

            /*
             * Concurrent duplicate insert protection.
             */
            log.warn(
                    "[PAYMENT SERVICE] Concurrent duplicate insert detected for requestId: {}",
                    requestId
            );

            Purchase existing =
                    purchaseRepository.findByRequestId(requestId)
                            .orElseThrow(() -> e);

            DeliveryReceipt receipt =
                    receiptService.findByRequestId(requestId)
                            .orElse(null);

            return buildExistingResponse(
                    existing,
                    receipt
            );
        }
    }

    /**
     * Builds a response for an already-settled request.
     */
    private PaymentResponseDto buildExistingResponse(
            Purchase purchase,
            DeliveryReceipt receipt
    ) {

        PaymentResponseDto response =
                new PaymentResponseDto();

        response.setRequestId(
                purchase.getRequestId()
        );

        response.setPurchaseId(
                purchase.getId()
        );

        response.setAgentId(
                purchase.getAgentId()
        );

        response.setProviderId(
                purchase.getProviderId()
        );

        response.setServiceId(
                purchase.getServiceId()
        );

        response.setAmountPaise(
                purchase.getAmountPaise()
        );

        response.setCurrency("INR");

        response.setStatus(
                purchase.getStatus()
        );

        response.setPaymentStatus(
                purchase.getPaymentStatus()
        );

        response.setTransactionHash(
                purchase.getTransactionHash()
        );

        response.setBlockchainMode(
                purchase.getBlockchainMode()
        );

        response.setEnforcementLayer(
                blockchainService.getEnforcementLayer()
        );

        if (receipt != null) {

            response.setDeliveredContent(
                    receipt.getDeliveredContent()
            );

            response.setContentHash(
                    receipt.getContentHash()
            );

            response.setReceiptId(
                    receipt.getReceiptId()
            );
        }

        response.setRetry(true);

        return response;
    }
}