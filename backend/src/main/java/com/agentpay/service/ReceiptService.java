package com.agentpay.service;

import com.agentpay.domain.DeliveryReceipt;
import com.agentpay.dto.VerifyReceiptResponseDto;
import com.agentpay.exception.ResourceNotFoundException;
import com.agentpay.repository.DeliveryReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private final DeliveryReceiptRepository receiptRepository;
    private final AuditService auditService;

    public ReceiptService(DeliveryReceiptRepository receiptRepository, AuditService auditService) {
        this.receiptRepository = receiptRepository;
        this.auditService = auditService;
    }

    public static String calculateSha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    @Transactional
    public DeliveryReceipt createReceipt(Long purchaseId, String requestId, String agentId, String providerId, String serviceId, Long amountPaise, String transactionHash, String deliveredContent) {
        String receiptId = "rcpt_" + requestId.toLowerCase().replace("-", "_");
        String contentHash = calculateSha256(deliveredContent);

        DeliveryReceipt receipt = new DeliveryReceipt(
                receiptId,
                purchaseId,
                requestId,
                agentId,
                providerId,
                serviceId,
                amountPaise,
                "INR",
                transactionHash,
                deliveredContent,
                contentHash,
                "VERIFIED"
        );

        DeliveryReceipt saved = receiptRepository.save(receipt);
        log.info("[RECEIPT SERVICE] Generated DeliveryReceipt {}. ContentHash: {}", receiptId, contentHash);
        return saved;
    }

    public DeliveryReceipt getByReceiptId(String receiptId) {
        return receiptRepository.findByReceiptId(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for id: " + receiptId));
    }

    public Optional<DeliveryReceipt> findByRequestId(String requestId) {
        return receiptRepository.findByRequestId(requestId);
    }

    public List<DeliveryReceipt> getAllReceipts() {
        return receiptRepository.findAllByOrderByDeliveredAtDesc();
    }

    @Transactional
    public VerifyReceiptResponseDto verifyReceipt(String receiptId, String contentOverride) {
        DeliveryReceipt receipt = getByReceiptId(receiptId);
        boolean isTamperTest = (contentOverride != null && !contentOverride.isBlank());
        String contentToTest = isTamperTest 
                ? contentOverride 
                : receipt.getDeliveredContent();

        String recalculatedHash = calculateSha256(contentToTest);
        boolean matches = recalculatedHash.equalsIgnoreCase(receipt.getContentHash());

        String status = matches ? "VERIFIED" : "FAILED";
        String message = matches 
                ? "Cryptographic proof matches: Delivered content SHA-256 equals recorded receipt hash."
                : "Tamper detection triggered: Content hash mismatch! Content was altered.";

        /*
         * Do not permanently change the legitimate receipt to FAILED merely because
         * an intentionally modified content override was supplied for verification testing.
         * The persistent verification status is updated when verifying the actual delivered content.
         */
        if (!isTamperTest || matches) {
            receipt.setVerificationStatus(status);
            receiptRepository.save(receipt);
        }

        String eventType = matches ? "RECEIPT_VERIFIED" : "RECEIPT_TAMPER_DETECTED";

        auditService.recordEvent(
                receipt.getRequestId(),
                receipt.getAgentId(),
                eventType,
                matches ? "SUCCESS" : "BLOCKED",
                "Receipt verification evaluated: " + status + " (" + message + ")",
                receipt.getAmountPaise(),
                receipt.getTransactionHash()
        );

        return new VerifyReceiptResponseDto(receiptId, recalculatedHash, receipt.getContentHash(), matches, status, message);
    }
}
