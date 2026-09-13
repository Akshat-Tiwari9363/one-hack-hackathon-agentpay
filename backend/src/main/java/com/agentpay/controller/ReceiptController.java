package com.agentpay.controller;

import com.agentpay.domain.DeliveryReceipt;
import com.agentpay.dto.ReceiptDto;
import com.agentpay.dto.VerifyReceiptResponseDto;
import com.agentpay.service.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public ResponseEntity<List<ReceiptDto>> getAllReceipts() {
        List<ReceiptDto> list = receiptService.getAllReceipts().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptDto> getReceiptById(@PathVariable String id) {
        DeliveryReceipt receipt = receiptService.getByReceiptId(id);
        return ResponseEntity.ok(toDto(receipt));
    }

    /**
     * Cryptographic verification endpoint that actually recalculates SHA-256 of the delivered content.
     * Optionally accepts {"contentOverride": "..."} in the request body to demonstrate tamper detection!
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<VerifyReceiptResponseDto> verifyReceipt(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        String contentOverride = (body != null) ? body.get("contentOverride") : null;
        VerifyReceiptResponseDto response = receiptService.verifyReceipt(id, contentOverride);
        return ResponseEntity.ok(response);
    }

    private ReceiptDto toDto(DeliveryReceipt r) {
        ReceiptDto dto = new ReceiptDto();
        dto.setId(r.getId());
        dto.setReceiptId(r.getReceiptId());
        dto.setPurchaseId(r.getPurchaseId());
        dto.setRequestId(r.getRequestId());
        dto.setAgentId(r.getAgentId());
        dto.setProviderId(r.getProviderId());
        dto.setServiceId(r.getServiceId());
        dto.setAmountPaise(r.getAmountPaise());
        dto.setCurrency(r.getCurrency());
        dto.setTransactionHash(r.getTransactionHash());
        dto.setDeliveredContent(r.getDeliveredContent());
        dto.setContentHash(r.getContentHash());
        dto.setVerificationStatus(r.getVerificationStatus());
        dto.setDeliveredAt(r.getDeliveredAt());
        return dto;
    }
}
