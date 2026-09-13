package com.agentpay.controller;

import com.agentpay.dto.SystemStatusDto;
import com.agentpay.service.ai.OllamaService;
import com.agentpay.service.blockchain.BlockchainService;
import com.agentpay.service.blockchain.DelegatingBlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {

    private final BlockchainService blockchainService;
    private final OllamaService ollamaService;

    public SystemStatusController(BlockchainService blockchainService, OllamaService ollamaService) {
        this.blockchainService = blockchainService;
        this.ollamaService = ollamaService;
    }

    @GetMapping("/status")
    public ResponseEntity<SystemStatusDto> getSystemStatus() {
        SystemStatusDto status = new SystemStatusDto();
        status.setBackendStatus("HEALTHY (Spring Boot 3.3.6 / Java 21)");
        status.setDatabaseStatus("ONLINE (H2 in-memory)");
        status.setOllamaStatus(ollamaService.isAvailable() ? "CONNECTED" : "OFFLINE (Heuristic Fallback Active)");
        status.setOllamaModel(ollamaService.getModelName());
        status.setBlockchainMode(blockchainService.getMode());

        status.setChainId(11155111L);

        if (blockchainService instanceof DelegatingBlockchainService delegator) {
            if ("SEPOLIA".equalsIgnoreCase(delegator.getMode())) {
                boolean connected = delegator.isSepoliaConnected() && delegator.isSepoliaContractValid() && delegator.hasSepoliaBytecode();
                status.setBlockchainConnected(connected);
                status.setContractAddress(delegator.getSepoliaContractAddress());
                status.setNetwork("Ethereum Sepolia");
            } else {
                status.setBlockchainConnected(true);
                status.setContractAddress(delegator.getSepoliaContractAddress());
                status.setNetwork("Deterministic Local Simulation");
            }
        } else {
            status.setBlockchainConnected(true);
            status.setContractAddress("0x0000000000000000000000000000000000000000");
            status.setNetwork("Deterministic Local Simulation");
        }

        status.setCurrency("INR (₹)");
        status.setTimestamp(Instant.now().toString());

        return ResponseEntity.ok(status);
    }
}
