package com.agentpay.controller;

import com.agentpay.domain.Payment;
import com.agentpay.dto.PaymentRequestDto;
import com.agentpay.dto.PaymentResponseDto;
import com.agentpay.exception.ResourceNotFoundException;
import com.agentpay.repository.PaymentRepository;
import com.agentpay.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> submitPayment(@Valid @RequestBody PaymentRequestDto request) {
        PaymentResponseDto response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for id: " + id));
        return ResponseEntity.ok(payment);
    }
}
