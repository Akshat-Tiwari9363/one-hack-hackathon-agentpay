package com.agentpay.controller;

import com.agentpay.domain.ServiceEntity;
import com.agentpay.dto.PaymentRequiredChallengeDto;
import com.agentpay.service.AuditService;
import com.agentpay.service.ProviderService;
import com.agentpay.service.blockchain.BlockchainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class ServiceResourceController {

    private final ProviderService providerService;
    private final AuditService auditService;
    private final BlockchainService blockchainService;

    public ServiceResourceController(ProviderService providerService, AuditService auditService, BlockchainService blockchainService) {
        this.providerService = providerService;
        this.auditService = auditService;
        this.blockchainService = blockchainService;
    }

    /**
     * HTTP 402 Payment Required endpoint.
     * When an autonomous agent attempts to access a protected service resource,
     * this endpoint issues a 402 Payment Required challenge with the authoritative price and payment quote.
     */
    @RequestMapping(value = "/{serviceId}/resource", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<PaymentRequiredChallengeDto> requestProtectedResource(
            @PathVariable String serviceId,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String customRequestId,
            @RequestBody(required = false) String rawBody) {

        String agent = (agentId != null && !agentId.isBlank()) ? agentId : "agent-demo-001";
        String reqId = (customRequestId != null && !customRequestId.isBlank()) 
                ? customRequestId 
                : "REQ-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ServiceEntity service = providerService.getServiceEntity(serviceId);

        auditService.recordEvent(
                reqId,
                agent,
                "SERVICE_REQUESTED",
                "INFO",
                "Agent requested protected resource for service '" + service.getName() + "' (" + serviceId + ")",
                service.getPricePaise(),
                null
        );

        auditService.recordEvent(
                reqId,
                agent,
                "PAYMENT_REQUIRED",
                "INFO",
                "HTTP 402 issued: Payment of ₹" + (service.getPricePaise() / 100.0) + " required before resource delivery.",
                service.getPricePaise(),
                null
        );

        PaymentRequiredChallengeDto challenge = new PaymentRequiredChallengeDto(
                reqId,
                service.getExternalId(),
                service.getProviderId(),
                service.getName(),
                service.getPricePaise(),
                blockchainService.getMode(),
                service.getQualityScore()
        );

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(challenge);
    }
}
