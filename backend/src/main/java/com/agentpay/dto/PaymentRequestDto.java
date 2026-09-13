package com.agentpay.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentRequestDto {

    @NotBlank(message = "requestId is required")
    private String requestId;

    @NotBlank(message = "agentId is required")
    private String agentId;

    @NotBlank(message = "providerId is required")
    private String providerId;

    @NotBlank(message = "serviceId is required")
    private String serviceId;

    // Optional override for adversarial / direct attack simulation
    private Long overridePricePaise;

    public PaymentRequestDto() {}

    public PaymentRequestDto(String requestId, String agentId, String providerId, String serviceId) {
        this.requestId = requestId;
        this.agentId = agentId;
        this.providerId = providerId;
        this.serviceId = serviceId;
    }

    public PaymentRequestDto(String requestId, String agentId, String providerId, String serviceId, Long overridePricePaise) {
        this.requestId = requestId;
        this.agentId = agentId;
        this.providerId = providerId;
        this.serviceId = serviceId;
        this.overridePricePaise = overridePricePaise;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public Long getOverridePricePaise() { return overridePricePaise; }
    public void setOverridePricePaise(Long overridePricePaise) { this.overridePricePaise = overridePricePaise; }
}
