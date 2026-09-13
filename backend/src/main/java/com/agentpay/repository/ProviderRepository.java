package com.agentpay.repository;

import com.agentpay.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
}
