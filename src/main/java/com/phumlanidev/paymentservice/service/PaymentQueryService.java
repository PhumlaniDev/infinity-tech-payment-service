package com.phumlanidev.paymentservice.service;

import com.phumlanidev.paymentservice.dto.PaymentResponseDto;
import com.phumlanidev.paymentservice.exception.payment.PaymentNotFoundException;
import com.phumlanidev.paymentservice.helper.PaymentMapper;
import com.phumlanidev.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentQueryService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;

  public PaymentResponseDto getPaymentById(Long paymentId) {
    log.info("Fetching payment by ID: {}", paymentId);
    return paymentRepository.findById(paymentId)
            .map(paymentMapper::toResponse)
            .orElseThrow(() -> new PaymentNotFoundException(
                    "Payment not found with ID: " + paymentId));
  }

  public PaymentResponseDto getPaymentByOrderId(Long orderId) {
    log.info("Fetching payment for orderId: {}", orderId);
    return paymentRepository.findByOrderId(orderId)
            .map(paymentMapper::toResponse)
            .orElseThrow(() -> new PaymentNotFoundException(
                    "Payment not found for orderId: " + orderId));
  }

  public List<PaymentResponseDto> getPaymentsByUserId(String userId) {
    log.info("Fetching payments for userId: {}", userId);
    return paymentRepository.findByUserId(userId)
            .stream()
            .map(paymentMapper::toResponse)
            .toList();
  }
}