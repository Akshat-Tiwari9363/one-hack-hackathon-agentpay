package com.agentpay.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary DTO of an entire multi-service autonomous run initiated by ONE START CLICK.
 * Contains the collection of every individual payment attempt, status, and Token accounting.
 */
public class AutonomousRunResponseDto {

    private String runId;
    private String agentId;
    private String userTask;
    private String status; // "COMPLETED", "PARTIAL", "FAILED"
    private Long budgetTokens;
    private Long spentTokens;
    private Long remainingTokens;
    private int totalAttempted;
    private int totalApproved;
    private int totalDenied;
    private List<AutonomousPaymentOutcomeDto> payments = new ArrayList<>();
    private String aiEngine;
    private String aiModel;
    private String aiSummary;
    private String denomination; // "TOKENS"

    public AutonomousRunResponseDto() {
        this.denomination = "TOKENS";
    }

    public AutonomousRunResponseDto(
            String runId,
            String agentId,
            String userTask,
            String status,
            Long budgetTokens,
            Long spentTokens,
            Long remainingTokens,
            int totalAttempted,
            int totalApproved,
            int totalDenied,
            List<AutonomousPaymentOutcomeDto> payments,
            String aiEngine,
            String aiModel,
            String aiSummary) {
        this.runId = runId;
        this.agentId = agentId;
        this.userTask = userTask;
        this.status = status;
        this.budgetTokens = budgetTokens;
        this.spentTokens = spentTokens;
        this.remainingTokens = remainingTokens;
        this.totalAttempted = totalAttempted;
        this.totalApproved = totalApproved;
        this.totalDenied = totalDenied;
        this.payments = payments != null ? payments : new ArrayList<>();
        this.aiEngine = aiEngine;
        this.aiModel = aiModel;
        this.aiSummary = aiSummary;
        this.denomination = "TOKENS";
    }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getUserTask() { return userTask; }
    public void setUserTask(String userTask) { this.userTask = userTask; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getBudgetTokens() { return budgetTokens; }
    public void setBudgetTokens(Long budgetTokens) { this.budgetTokens = budgetTokens; }

    public Long getSpentTokens() { return spentTokens; }
    public void setSpentTokens(Long spentTokens) { this.spentTokens = spentTokens; }

    public Long getRemainingTokens() { return remainingTokens; }
    public void setRemainingTokens(Long remainingTokens) { this.remainingTokens = remainingTokens; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public int getTotalApproved() { return totalApproved; }
    public void setTotalApproved(int totalApproved) { this.totalApproved = totalApproved; }

    public int getTotalDenied() { return totalDenied; }
    public void setTotalDenied(int totalDenied) { this.totalDenied = totalDenied; }

    public List<AutonomousPaymentOutcomeDto> getPayments() { return payments; }
    public void setPayments(List<AutonomousPaymentOutcomeDto> payments) { this.payments = payments; }

    public String getAiEngine() { return aiEngine; }
    public void setAiEngine(String aiEngine) { this.aiEngine = aiEngine; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public String getDenomination() { return denomination; }
    public void setDenomination(String denomination) { this.denomination = denomination; }
}
