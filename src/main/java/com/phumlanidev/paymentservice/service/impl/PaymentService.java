package com.phumlanidev.paymentservice.service.impl;


import com.phumlanidev.commonevents.events.payment.PaymentCompletedEvent;
import com.phumlanidev.commonevents.events.payment.PaymentFailedEvent;
import com.phumlanidev.commonevents.events.payment.PaymentInitiatedEvent;
import com.phumlanidev.paymentservice.enums.PaymentStatus;
import com.phumlanidev.paymentservice.event.publisher.PublishPaymentFailedEvent;
import com.phumlanidev.paymentservice.event.publisher.PublishPaymentInitiatedEvent;
import com.phumlanidev.paymentservice.event.publisher.PublisherPaymentCompletedEvent;
import com.phumlanidev.paymentservice.model.Payment;
import com.phumlanidev.paymentservice.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final AuditLogServiceImpl auditLogService;
  private final PublishPaymentFailedEvent paymentFailedEvent;
  private final PublishPaymentInitiatedEvent paymentInitiatedEvent;
  private final PaymentRepository paymentRepository;
  private final PublisherPaymentCompletedEvent paymentCompletedEvent;

  @Transactional
  public Payment handlePaymentSuccess(Long paymentId, String transactionId, String userEmail) {
    try {
      Payment payment = paymentRepository.findById(paymentId)
              .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

//      String userEmail = securityUtils.getCurrentEmail();
      payment.setPaymentStatus(PaymentStatus.COMPLETED);
      payment.setTransactionId(transactionId);

      Payment updatedPayment = paymentRepository.save(payment);

      log.info("✅ Payment marked as COMPLETED for order ID: {} with transaction: {}", payment.getOrderId(), transactionId);

      PaymentCompletedEvent event = PaymentCompletedEvent.builder()
              .orderId(payment.getOrderId())
              .userId(payment.getUserId())
              .toEmail(userEmail)
              .currency(payment.getCurrency())
              .totalAmount(payment.getAmount())
              .transactionId(transactionId)
              .timestamp(Instant.now())
              .build();

      try {
        paymentCompletedEvent.publishPaymentCompleted(event);
      } catch (Exception e) {
        log.error("Failed tp publish PaymentCompletedEvent for order ID : {}", payment.getOrderId(), e);
        throw new RuntimeException("Event publish failed", e);
      }

      logAudit("PAYMENT_COMPLETED", "Payment completed for order ID: " + payment.getOrderId());
      return updatedPayment;
    } catch (RuntimeException e) {
      log.error("Error in handlePaymentSuccess for payment ID: {}", paymentId, e);
      throw e;
    }
  }

//  @Transactional
//  public Payment handlePaymentSuccess(PaymentRequestEvent requestEvent, String transactionId) {
//    Payment payment = paymentRepository.findByOrderId(requestEvent.getOrderId())
//            .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + requestEvent.getOrderId()));
//    payment.setPaymentStatus(PaymentStatus.COMPLETED);
//    payment.setTransactionId(transactionId);
//    Payment updatedPayment = paymentRepository.save(payment);
//
//    log.info("✅ Payment marked as COMPLETED for order ID: {} with transaction: {}", requestEvent.getOrderId(), transactionId);
//
//    PaymentCompletedEvent event = PaymentCompletedEvent.builder()
//            .orderId(payment.getOrderId())
//            .userId(String.valueOf(requestEvent.getUserId()))
//            .toEmail(requestEvent.getToEmail())
//            .currency(payment.getCurrency())
//            .totalAmount(payment.getAmount())
//            .transactionId(payment.getTransactionId())
//            .timestamp(java.time.Instant.now())
//            .build();
//
//    paymentCompletedEvent.publishPaymentCompleted(event);
//
//    logAudit("PAYMENT_COMPLETED", "Payment completed for order ID: " + requestEvent.getOrderId());
//    return updatedPayment;
//  }

  @Transactional
  public Payment createPendingPayment(
          Long orderId, String userId, BigDecimal amount, String currency, String paymentMethod) {
    Payment p = Payment.builder()
            .orderId(orderId)
            .userId(userId)
            .amount(amount)
            .currency(currency)
            .paymentStatus(PaymentStatus.PENDING)
            .paymentMethod(paymentMethod)
            .build();

    Payment saved = paymentRepository.save(p);
    log.info("💾 Pending payment record created with ID: {}", saved.getPayment_id());
    paymentInitiatedEvent.publishPaymentInitiated(PaymentInitiatedEvent.builder()
            .paymentId(saved.getPayment_id())
            .orderId(orderId)
            .userId(saved.getUserId())
            .amount(amount)
            .currency(currency)
            .paymentMethod(paymentMethod)
            .timestamp(Instant.now())
            .build());

    logAudit("PAYMENT_PENDING", "Pending payment created for order ID: " + orderId);
    return saved;
  }

  public Payment markPaymentAsCompleted(Long paymentId, String transactionId) {
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found for transaction ID: " + transactionId));
    if (PaymentStatus.COMPLETED.equals(payment.getPaymentStatus())) {
      return payment;
    }
    payment.setPaymentStatus(PaymentStatus.COMPLETED);
    payment.setTransactionId(transactionId);
    payment.setUpdatedAt(Instant.now());
    payment.setUpdatedBy("SYSTEM");
    Payment updatedPayment = paymentRepository.save(payment);
    log.info("✅ Payment marked as COMPLETED for order ID: {} with transaction: {}", payment.getOrderId(), transactionId);
    logAudit("PAYMENT_COMPLETED", "Payment completed for order ID: " + payment.getOrderId());

    paymentCompletedEvent.publishPaymentCompleted(PaymentCompletedEvent.builder()
            .paymentId(updatedPayment.getPayment_id())
            .orderId(updatedPayment.getOrderId())
            .userId(updatedPayment.getUserId())
            .totalAmount(updatedPayment.getAmount())
            .currency(updatedPayment.getCurrency())
            .transactionId(updatedPayment.getTransactionId())
            .timestamp(Instant.now())
            .build());
    return updatedPayment;
  }

  public Payment markPaymentFailed(Long paymentId, String reason) {
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found for ID: " + paymentId));
    if (PaymentStatus.FAILED.equals(payment.getPaymentStatus())) {
      return payment;
    }
    payment.setPaymentStatus(PaymentStatus.FAILED);
    payment.setUpdatedAt(Instant.now());
    payment.setUpdatedBy("SYSTEM");
    log.info("❌ Payment marked as FAILED for order ID: {}. Reason: {}", payment.getOrderId(), reason);
    Payment updatedPayment = paymentRepository.save(payment);
    logAudit("PAYMENT_FAILED", "Payment failed for order ID: " + payment.getOrderId() + ". Reason: " + reason);

    paymentFailedEvent.publishPaymentFailed(PaymentFailedEvent.builder()
            .paymentId(updatedPayment.getPayment_id())
            .orderId(updatedPayment.getOrderId())
            .userId(updatedPayment.getUserId())
            .totalAmount(updatedPayment.getAmount())
            .currency(updatedPayment.getCurrency())
            .transactionId(updatedPayment.getTransactionId())
            .timestamp(Instant.now())
            .build());
    return updatedPayment;
  }

//  @Transactional
//  public Payment initialPayment(Long orderId, String userId, String currency, PaymentMethod paymentMethod,
//                                String transactionId, BigDecimal amount) {
//    Payment payment = Payment.builder()
//            .orderId((orderId))
//            .userId(userId)
//            .amount(amount)
//            .currency(currency)
//            .paymentStatus(PaymentStatus.PENDING)
//            .paymentMethod(paymentMethod)
//            .transactionId(transactionId)
//            .build();
//
//    PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
//            .orderId(payment.getOrderId())
//            .userId(payment.getUserId())
//            .amount(payment.getAmount())
//            .transactionId(payment.getTransactionId())
//            .build();
//    paymentInitiatedEvent.publishPaymentInitiated(event);
//    Payment savedPayment = paymentRepository.save(payment);
//    log.info("💾 Initial payment record created with ID: {}", savedPayment.getId());
//
//    logAudit("PAYMENT_INITIATED", "Payment initiated for order ID: " + orderId);
//    return savedPayment;
//  }
//
//  @Transactional
//  public Payment confirmPayment(String transactionId) {
//    Payment payment = paymentRepository.findByTransactionId(transactionId).orElseThrow(
//            () -> new RuntimeException("Payment not found for transaction ID: " + transactionId)
//    );
//
//    payment.setPaymentStatus(PaymentStatus.COMPLETED);
//    Payment updatedPayment = paymentRepository.save(payment);
//    log.info("✅ Payment confirmed for transaction ID: {}", transactionId);
//
//    PaymentCompletedEvent event = PaymentCompletedEvent.builder()
//            .orderId(payment.getOrderId())
//            .toEmail("") // Email can be fetched from user service if needed
//            .currency(payment.getCurrency())
//            .totalAmount(payment.getAmount())
//            .transactionId(payment.getTransactionId())
//            .timestamp(java.time.Instant.now())
//            .build();
//
//    paymentCompletedEvent.publishPaymentCompleted(event);
//
//    logAudit("PAYMENT_CONFIRMED", "Payment confirmed for transaction ID: " + transactionId);
//    return updatedPayment;
//  }
//
//  @Transactional
//  public Payment failPayment(String transactionId) {
//    Payment payment = paymentRepository.findByTransactionId(transactionId)
//            .orElseThrow(() -> new RuntimeException("Payment not found for transaction ID: " + transactionId));
//    payment.setPaymentStatus(PaymentStatus.FAILED);
//    Payment updatedPayment = paymentRepository.save(payment);
//    log.warn("❌ Payment failed for transaction ID: {}.", transactionId);
//
//    PaymentFailedEvent event = PaymentFailedEvent.builder()
//            .orderId(payment.getOrderId())
//            .userId(String.valueOf(payment.getUserId()))
//            .amount(payment.getAmount())
//            .transactionId(payment.getTransactionId())
//            .build();
//
//    paymentFailedEvent.publishPaymentFailed(event);
//
//    logAudit("PAYMENT_FAILED", "Payment failed for transaction ID: " + transactionId);
//    return updatedPayment;
//  }

  private void logAudit(String action, String details) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";

    auditLogService.log(
            action,
            username,
            "SYSTEM",
            details
    );
  }
}
