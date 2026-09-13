package com.agentpay.dto;

public class AgentDto {
    private Long id;
    private String externalId;
    private String name;
    private String walletAddress;
    private Long budgetPaise;
    private Double budgetINR;
    private Long spentPaise;
    private Double spentINR;
    private Long remainingPaise;
    private Double remainingINR;
    private String status;
    private String currency;
    private String blockchainMode;

    public AgentDto() {
        this.currency = "INR";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public Long getBudgetPaise() { return budgetPaise; }
    public void setBudgetPaise(Long budgetPaise) {
        this.budgetPaise = budgetPaise;
        this.budgetINR = budgetPaise != null ? budgetPaise / 100.0 : 0.0;
    }

    public Double getBudgetINR() { return budgetINR; }
    public void setBudgetINR(Double budgetINR) { this.budgetINR = budgetINR; }

    public Long getSpentPaise() { return spentPaise; }
    public void setSpentPaise(Long spentPaise) {
        this.spentPaise = spentPaise;
        this.spentINR = spentPaise != null ? spentPaise / 100.0 : 0.0;
    }

    public Double getSpentINR() { return spentINR; }
    public void setSpentINR(Double spentINR) { this.spentINR = spentINR; }

    public Long getRemainingPaise() { return remainingPaise; }
    public void setRemainingPaise(Long remainingPaise) {
        this.remainingPaise = remainingPaise;
        this.remainingINR = remainingPaise != null ? remainingPaise / 100.0 : 0.0;
    }

    public Double getRemainingINR() { return remainingINR; }
    public void setRemainingINR(Double remainingINR) { this.remainingINR = remainingINR; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getBlockchainMode() { return blockchainMode; }
    public void setBlockchainMode(String blockchainMode) { this.blockchainMode = blockchainMode; }
}
