package com.agentpay.repository;

import com.agentpay.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRequestId(String requestId);
    boolean existsByRequestId(String requestId);
    List<Payment> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
