package com.agentpay.dto;

public class BudgetDto {
    private String agentId;
    private Long budgetPaise;
    private Double budgetINR;
    private Long spentPaise;
    private Double spentINR;
    private Long remainingPaise;
    private Double remainingINR;
    private Double utilizationPercent;
    private String currency;
    private String status;
    private boolean enforced;
    private String blockchainMode;
    private String enforcementLayer;

    public BudgetDto() {
        this.currency = "INR";
        this.enforced = true;
    }

    public BudgetDto(String agentId, Long budgetPaise, Long spentPaise, String status, boolean enforced, String blockchainMode, String enforcementLayer) {
        this.agentId = agentId;
        this.budgetPaise = budgetPaise;
        this.budgetINR = budgetPaise != null ? budgetPaise / 100.0 : 0.0;
        this.spentPaise = spentPaise;
        this.spentINR = spentPaise != null ? spentPaise / 100.0 : 0.0;
        long rem = (budgetPaise != null && spentPaise != null) ? Math.max(0, budgetPaise - spentPaise) : 0L;
        this.remainingPaise = rem;
        this.remainingINR = rem / 100.0;
        this.utilizationPercent = (budgetPaise != null && budgetPaise > 0) ? Math.min(100.0, (spentPaise * 100.0) / budgetPaise) : 0.0;
        this.currency = "INR";
        this.status = status;
        this.enforced = enforced;
        this.blockchainMode = blockchainMode;
        this.enforcementLayer = enforcementLayer;
    }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public Long getBudgetPaise() { return budgetPaise; }
    public void setBudgetPaise(Long budgetPaise) { this.budgetPaise = budgetPaise; }

    public Double getBudgetINR() { return budgetINR; }
    public void setBudgetINR(Double budgetINR) { this.budgetINR = budgetINR; }

    public Long getSpentPaise() { return spentPaise; }
    public void setSpentPaise(Long spentPaise) { this.spentPaise = spentPaise; }

    public Double getSpentINR() { return spentINR; }
    public void setSpentINR(Double spentINR) { this.spentINR = spentINR; }

    public Long getRemainingPaise() { return remainingPaise; }
    public void setRemainingPaise(Long remainingPaise) { this.remainingPaise = remainingPaise; }

    public Double getRemainingINR() { return remainingINR; }
    public void setRemainingINR(Double remainingINR) { this.remainingINR = remainingINR; }

    public Double getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(Double utilizationPercent) { this.utilizationPercent = utilizationPercent; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isEnforced() { return enforced; }
    public void setEnforced(boolean enforced) { this.enforced = enforced; }

    public String getBlockchainMode() { return blockchainMode; }
    public void setBlockchainMode(String blockchainMode) { this.blockchainMode = blockchainMode; }

    public String getEnforcementLayer() { return enforcementLayer; }
    public void setEnforcementLayer(String enforcementLayer) { this.enforcementLayer = enforcementLayer; }
}
