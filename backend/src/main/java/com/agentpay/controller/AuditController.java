package com.agentpay.controller;

import com.agentpay.domain.AuditEvent;
import com.agentpay.dto.AuditEventDto;
import com.agentpay.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditEventDto>> getAllAuditEvents() {
        List<AuditEventDto> list = auditService.getAllEvents().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<List<AuditEventDto>> getAuditTimeline(@PathVariable String requestId) {
        List<AuditEventDto> list = auditService.getTimelineByRequestId(requestId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private AuditEventDto toDto(AuditEvent e) {
        AuditEventDto dto = new AuditEventDto();
        dto.setId(e.getId());
        dto.setRequestId(e.getRequestId());
        dto.setAgentId(e.getAgentId());
        dto.setEventType(e.getEventType());
        dto.setStatus(e.getStatus());
        dto.setMessage(e.getMessage());
        dto.setAmountPaise(e.getAmountPaise());
        dto.setCurrency(e.getCurrency());
        dto.setTransactionHash(e.getTransactionHash());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
