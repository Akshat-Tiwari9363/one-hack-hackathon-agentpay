package com.agentpay.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 64)
    private String externalId;

    @Column(name = "provider_id", nullable = false, length = 64)
    private String providerId; // externalId of provider

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // e.g. "TRANSLATION", "MARKETPLACE", "COMPUTE"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_paise", nullable = false)
    private Long pricePaise; // 30000 = ₹300, 50000 = ₹500, 20000 = ₹200, 80000 = ₹800

    @Column(nullable = false, length = 8)
    private String currency; // Always "INR"

    @Column(name = "quality_score", nullable = false)
    private Integer qualityScore;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ServiceEntity() {}

    public ServiceEntity(String externalId, String providerId, String name, String type, String description, Long pricePaise, String currency, Integer qualityScore, String status) {
        this.externalId = externalId;
        this.providerId = providerId;
        this.name = name;
        this.type = type;
        this.description = description;
        this.pricePaise = pricePaise;
        this.currency = currency;
        this.qualityScore = qualityScore;
        this.status = status;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (currency == null) currency = "INR";
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
    public void setPricePaise(Long pricePaise) { this.pricePaise = pricePaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
