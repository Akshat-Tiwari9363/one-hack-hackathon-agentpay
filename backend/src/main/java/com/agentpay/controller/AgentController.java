package com.agentpay.controller;

import com.agentpay.domain.Agent;
import com.agentpay.dto.AgentDto;
import com.agentpay.dto.BudgetDto;
import com.agentpay.exception.ResourceNotFoundException;
import com.agentpay.repository.AgentRepository;
import com.agentpay.service.blockchain.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentRepository agentRepository;
    private final BlockchainService blockchainService;

    public AgentController(AgentRepository agentRepository, BlockchainService blockchainService) {
        this.agentRepository = agentRepository;
        this.blockchainService = blockchainService;
    }

    @GetMapping
    public ResponseEntity<List<AgentDto>> getAllAgents() {
        List<AgentDto> list = agentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentDto> getAgentById(@PathVariable String id) {
        Agent agent = agentRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + id));
        return ResponseEntity.ok(toDto(agent));
    }

    @GetMapping("/{id}/budget")
    public ResponseEntity<BudgetDto> getAgentBudget(@PathVariable String id) {
        Agent agent = agentRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + id));

        Long budgetPaise = blockchainService.getBudget(agent.getExternalId());
        if (budgetPaise == null || budgetPaise == 0) budgetPaise = agent.getBudgetPaise();
        Long spentPaise = blockchainService.getSpent(agent.getExternalId());

        BudgetDto dto = new BudgetDto(
                agent.getExternalId(),
                budgetPaise,
                spentPaise,
                agent.getStatus(),
                true,
                blockchainService.getMode(),
                blockchainService.getEnforcementLayer()
        );

        return ResponseEntity.ok(dto);
    }

    private AgentDto toDto(Agent agent) {
        AgentDto dto = new AgentDto();
        dto.setId(agent.getId());
        dto.setExternalId(agent.getExternalId());
        dto.setName(agent.getName());
        dto.setWalletAddress(agent.getWalletAddress());

        Long budgetPaise = blockchainService.getBudget(agent.getExternalId());
        if (budgetPaise == null || budgetPaise == 0) budgetPaise = agent.getBudgetPaise();
        Long spentPaise = blockchainService.getSpent(agent.getExternalId());
        Long remPaise = blockchainService.getRemaining(agent.getExternalId());

        dto.setBudgetPaise(budgetPaise);
        dto.setSpentPaise(spentPaise);
        dto.setRemainingPaise(remPaise);
        dto.setStatus(agent.getStatus());
        dto.setBlockchainMode(blockchainService.getMode());
        return dto;
    }
}
