package com.agentpay.controller;

import com.agentpay.domain.Purchase;
import com.agentpay.dto.PurchaseDto;
import com.agentpay.exception.ResourceNotFoundException;
import com.agentpay.repository.PurchaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;

    public PurchaseController(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseDto>> getAllPurchases() {
        List<PurchaseDto> list = purchaseRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseDto> getPurchaseById(@PathVariable Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found for id: " + id));
        return ResponseEntity.ok(toDto(purchase));
    }

    @GetMapping("/latest")
    public ResponseEntity<PurchaseDto> getLatestPurchase() {
        List<Purchase> purchases = purchaseRepository.findAllByOrderByCreatedAtDesc();
        if (purchases.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(toDto(purchases.get(0)));
    }

    private PurchaseDto toDto(Purchase p) {
        PurchaseDto dto = new PurchaseDto();
        dto.setId(p.getId());
        dto.setRequestId(p.getRequestId());
        dto.setAgentId(p.getAgentId());
        dto.setProviderId(p.getProviderId());
        dto.setServiceId(p.getServiceId());
        dto.setAmountPaise(p.getAmountPaise());
        dto.setCurrency(p.getCurrency());
        dto.setStatus(p.getStatus());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setTransactionHash(p.getTransactionHash());
        dto.setBlockchainMode(p.getBlockchainMode());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
