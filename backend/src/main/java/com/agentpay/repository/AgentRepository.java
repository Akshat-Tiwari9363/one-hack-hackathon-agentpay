package com.agentpay.repository;

import com.agentpay.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
}
