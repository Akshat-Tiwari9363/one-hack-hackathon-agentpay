package com.agentpay.dto;

public class DashboardSummaryDto {
    private Double budgetINR;
    private Double spentINR;
    private Double remainingINR;
    private Double utilizationPercent;
    private Long budgetPaise;
    private Long spentPaise;
    private Long remainingPaise;
    private long successfulTxnsCount;
    private long blockedAttacksCount;
    private long totalPurchasesCount;
    private long providersCount;
    private String agentName;
    private String agentStatus;
    private String agentWallet;
    private String blockchainMode;
    private String enforcementLayer;
    private String aiEngine;
    private String aiModel;
    private String currency;
    private Double dailyCapINR;
    private boolean budgetEnforced;

    public DashboardSummaryDto() {
        this.currency = "INR";
        this.budgetEnforced = true;
    }

    public Double getBudgetINR() { return budgetINR; }
    public void setBudgetINR(Double budgetINR) { this.budgetINR = budgetINR; }

    public Double getSpentINR() { return spentINR; }
    public void setSpentINR(Double spentINR) { this.spentINR = spentINR; }

    public Double getRemainingINR() { return remainingINR; }
    public void setRemainingINR(Double remainingINR) { this.remainingINR = remainingINR; }

    public Double getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(Double utilizationPercent) { this.utilizationPercent = utilizationPercent; }

    public Long getBudgetPaise() { return budgetPaise; }
    public void setBudgetPaise(Long budgetPaise) { this.budgetPaise = budgetPaise; }

    public Long getSpentPaise() { return spentPaise; }
    public void setSpentPaise(Long spentPaise) { this.spentPaise = spentPaise; }

    public Long getRemainingPaise() { return remainingPaise; }
    public void setRemainingPaise(Long remainingPaise) { this.remainingPaise = remainingPaise; }

    public long getSuccessfulTxnsCount() { return successfulTxnsCount; }
    public void setSuccessfulTxnsCount(long successfulTxnsCount) { this.successfulTxnsCount = successfulTxnsCount; }

    public long getBlockedAttacksCount() { return blockedAttacksCount; }
    public void setBlockedAttacksCount(long blockedAttacksCount) { this.blockedAttacksCount = blockedAttacksCount; }

    public long getTotalPurchasesCount() { return totalPurchasesCount; }
    public void setTotalPurchasesCount(long totalPurchasesCount) { this.totalPurchasesCount = totalPurchasesCount; }

    public long getProvidersCount() { return providersCount; }
    public void setProvidersCount(long providersCount) { this.providersCount = providersCount; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getAgentStatus() { return agentStatus; }
    public void setAgentStatus(String agentStatus) { this.agentStatus = agentStatus; }

    public String getAgentWallet() { return agentWallet; }
    public void setAgentWallet(String agentWallet) { this.agentWallet = agentWallet; }

    public String getBlockchainMode() { return blockchainMode; }
    public void setBlockchainMode(String blockchainMode) { this.blockchainMode = blockchainMode; }

    public String getEnforcementLayer() { return enforcementLayer; }
    public void setEnforcementLayer(String enforcementLayer) { this.enforcementLayer = enforcementLayer; }

    public String getAiEngine() { return aiEngine; }
    public void setAiEngine(String aiEngine) { this.aiEngine = aiEngine; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Double getDailyCapINR() { return dailyCapINR; }
    public void setDailyCapINR(Double dailyCapINR) { this.dailyCapINR = dailyCapINR; }

    public boolean isBudgetEnforced() { return budgetEnforced; }
    public void setBudgetEnforced(boolean budgetEnforced) { this.budgetEnforced = budgetEnforced; }
}
