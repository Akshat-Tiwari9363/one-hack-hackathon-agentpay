package com.agentpay.repository;

import com.agentpay.domain.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    Optional<ServiceEntity> findByExternalId(String externalId);
    List<ServiceEntity> findByProviderId(String providerId);
    boolean existsByExternalId(String externalId);
}
