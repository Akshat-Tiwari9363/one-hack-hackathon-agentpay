package com.agentpay.repository;

import com.agentpay.domain.DeliveryReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryReceiptRepository extends JpaRepository<DeliveryReceipt, Long> {
    Optional<DeliveryReceipt> findByReceiptId(String receiptId);
    Optional<DeliveryReceipt> findByRequestId(String requestId);
    Optional<DeliveryReceipt> findByPurchaseId(Long purchaseId);
    List<DeliveryReceipt> findAllByOrderByDeliveredAtDesc();
}
