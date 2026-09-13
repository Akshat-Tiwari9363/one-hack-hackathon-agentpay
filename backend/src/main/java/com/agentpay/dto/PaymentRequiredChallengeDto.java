package com.agentpay.dto;

public class PaymentRequiredChallengeDto {
    private String requestId;
    private String serviceId;
    private String providerId;
    private String serviceName;
    private Long amountPaise;
    private Double amount;
    private String currency;
    private boolean paymentRequired;
    private String paymentNetwork;
    private Integer qualityScore;

    public PaymentRequiredChallengeDto() {
        this.paymentRequired = true;
        this.currency = "INR";
    }

    public PaymentRequiredChallengeDto(String requestId, String serviceId, String providerId, String serviceName, Long amountPaise, String paymentNetwork, Integer qualityScore) {
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.providerId = providerId;
        this.serviceName = serviceName;
        this.amountPaise = amountPaise;
        this.amount = amountPaise != null ? amountPaise / 100.0 : null;
        this.currency = "INR";
        this.paymentRequired = true;
        this.paymentNetwork = paymentNetwork;
        this.qualityScore = qualityScore;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) {
        this.amountPaise = amountPaise;
        this.amount = amountPaise != null ? amountPaise / 100.0 : null;
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isPaymentRequired() { return paymentRequired; }
    public void setPaymentRequired(boolean paymentRequired) { this.paymentRequired = paymentRequired; }

    public String getPaymentNetwork() { return paymentNetwork; }
    public void setPaymentNetwork(String paymentNetwork) { this.paymentNetwork = paymentNetwork; }

    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }
}
