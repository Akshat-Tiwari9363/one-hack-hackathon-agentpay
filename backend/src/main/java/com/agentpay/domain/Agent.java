package com.agentpay.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 64)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(name = "budget_paise", nullable = false)
    private Long budgetPaise; // e.g. 100000 for ₹1,000

    @Column(nullable = false)
    private String status; // ACTIVE, PAUSED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Agent() {}

    public Agent(String externalId, String name, String walletAddress, Long budgetPaise, String status) {
        this.externalId = externalId;
        this.name = name;
        this.walletAddress = walletAddress;
        this.budgetPaise = budgetPaise;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public Long getBudgetPaise() { return budgetPaise; }
    public void setBudgetPaise(Long budgetPaise) { this.budgetPaise = budgetPaise; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
