package com.agentpay.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "providers")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 64)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "quality_score", nullable = false)
    private Integer qualityScore; // e.g. 90, 97, 82

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Provider() {}

    public Provider(String externalId, String name, String description, Integer qualityScore, String status) {
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.qualityScore = qualityScore;
        this.status = status;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
