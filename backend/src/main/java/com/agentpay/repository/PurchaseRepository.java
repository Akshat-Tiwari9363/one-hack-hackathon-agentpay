package com.agentpay.repository;

import com.agentpay.domain.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByRequestId(String requestId);
    boolean existsByRequestId(String requestId);
    List<Purchase> findByAgentIdOrderByCreatedAtDesc(String agentId);
    List<Purchase> findAllByOrderByCreatedAtDesc();
    long countByPaymentStatus(String paymentStatus);
}
