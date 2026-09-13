package com.agentpay.dto;

import java.util.ArrayList;
import java.util.List;

public class ProviderDto {
    private Long id;
    private String externalId;
    private String name;
    private String description;
    private Integer qualityScore;
    private String status;
    private List<ServiceDto> services = new ArrayList<>();

    public ProviderDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ServiceDto> getServices() { return services; }
    public void setServices(List<ServiceDto> services) { this.services = services; }
}
