package com.agentpay.dto;

public class AutonomousPurchaseRequestDto {
    private String agentId;
    private String prompt;
    private String targetServiceType; // e.g. "TRANSLATION"
    private boolean simulateAdversarialOverspend; // If true, instructs AI to pick the expensive ₹800 service to test boundary

    private Integer maxActions;
    private String customRequestId;

    public AutonomousPurchaseRequestDto() {}

    public AutonomousPurchaseRequestDto(String agentId, String prompt, String targetServiceType, boolean simulateAdversarialOverspend) {
        this.agentId = agentId;
        this.prompt = prompt;
        this.targetServiceType = targetServiceType;
        this.simulateAdversarialOverspend = simulateAdversarialOverspend;
    }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getTargetServiceType() { return targetServiceType; }
    public void setTargetServiceType(String targetServiceType) { this.targetServiceType = targetServiceType; }

    public boolean isSimulateAdversarialOverspend() { return simulateAdversarialOverspend; }
    public void setSimulateAdversarialOverspend(boolean simulateAdversarialOverspend) { this.simulateAdversarialOverspend = simulateAdversarialOverspend; }

    public Integer getMaxActions() { return maxActions; }
    public void setMaxActions(Integer maxActions) { this.maxActions = maxActions; }

    public String getCustomRequestId() { return customRequestId; }
    public void setCustomRequestId(String customRequestId) { this.customRequestId = customRequestId; }
}
