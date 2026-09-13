package com.agentpay.dto;

public class ServiceDto {
    private Long id;
    private String externalId;
    private String providerId;
    private String name;
    private String type;
    private String description;
    private Long pricePaise;
    private Double priceINR;
    private String currency;
    private Integer qualityScore;
    private String status;

    public ServiceDto() {
        this.currency = "INR";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPricePaise() { return pricePaise; }
    public void setPricePaise(Long pricePaise) {
        this.pricePaise = pricePaise;
        this.priceINR = pricePaise != null ? pricePaise / 100.0 : 0.0;
    }

    public Double getPriceINR() { return priceINR; }
    public void setPriceINR(Double priceINR) { this.priceINR = priceINR; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
