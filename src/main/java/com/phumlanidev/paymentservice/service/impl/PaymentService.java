package com.phumlanidev.paymentservice.service.impl;


import com.phumlanidev.commonevents.events.payment.PaymentCompletedEvent;
import com.phumlanidev.commonevents.events.payment.PaymentFailedEvent;
import com.phumlanidev.commonevents.events.payment.PaymentInitiatedEvent;
import com.phumlanidev.paymentservice.clinet.OrderServiceClient;
import com.phumlanidev.paymentservice.dto.OrderDto;
import com.phumlanidev.paymentservice.enums.PaymentStatus;
import com.phumlanidev.paymentservice.event.publisher.PublishPaymentFailedEvent;
import com.phumlanidev.paymentservice.event.publisher.PublishPaymentInitiatedEvent;
import com.phumlanidev.paymentservice.event.publisher.PublisherPaymentCompletedEvent;
import com.phumlanidev.paymentservice.model.Payment;
import com.phumlanidev.paymentservice.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final AuditLogServiceImpl auditLogService;
  private final PublishPaymentFailedEvent paymentFailedEvent;
  private final PublishPaymentInitiatedEvent paymentInitiatedEvent;
  private final PublisherPaymentCompletedEvent paymentCompletedEvent;
  private final OrderServiceClient orderServiceClient;
  private final PaymentRepository paymentRepository;

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

    logAudit("PAYMENT_PENDING", saved.getUserId(), "Pending payment created for order ID: " + orderId);
    return saved;
  }

  @Transactional
  public Payment completePayment(
          Long paymentId,
          String transactionId,
          String userEmail,
          String customerName) {

    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException(
                    "Payment not found with ID: " + paymentId));

    if (PaymentStatus.COMPLETED.equals(payment.getPaymentStatus())) {
      log.warn("Payment {} is already COMPLTED - skiping", paymentId);
      return payment;
    }

    payment.setPaymentStatus(PaymentStatus.COMPLETED);
    payment.setTransactionId(transactionId);
    payment.setUpdatedAt(Instant.now());
    payment.setUpdatedBy("SYSTEM");

    Payment saved = paymentRepository.save(payment);
    log.info("Payment {} marked COMPLETED for orderId: {} |  stripeId: {}",
            paymentId, payment.getOrderId(), transactionId);


    List<PaymentCompletedEvent.InvoiceItemDto> invoiceItems =
            buildInvoiceItems(payment.getOrderId());

    PaymentCompletedEvent event = PaymentCompletedEvent.builder()
            .paymentId(saved.getPayment_id())
            .orderId(saved.getOrderId())
            .userId(saved.getUserId())
            .toEmail(userEmail)
            .customerName(customerName)
            .totalAmount(saved.getAmount())
            .currency(saved.getCurrency())
            .paymentMethod(saved.getPaymentMethod())
            .invoiceItems(invoiceItems)
            .timestamp(Instant.now())
            .build();

    try {
      paymentCompletedEvent.publishPaymentCompleted(event);
      log.info("PaymentCompletedEvent published for orderId: {}", saved.getOrderId());
    } catch (Exception e) {
      log.error("Failed to publish PaymentCompletedEvent for orderId: {}", saved.getOrderId(), e);
      throw new RuntimeException("Event publish failed", e);
    }

    logAudit("PAYMENT_COMPLETED", saved.getUserId(),
            "Payment completed for orderId: " + saved.getOrderId());

    return saved;
  }

  @Transactional
  public Payment markPaymentFailed(Long paymentId, String reason) {
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException(
                    "Payment not found with ID: " + paymentId));

    if (PaymentStatus.FAILED.equals(payment.getPaymentStatus())) {
      log.warn("Payment {} is already FAILED - skiping", paymentId);
      return payment;
    }

    payment.setPaymentStatus(PaymentStatus.FAILED);
    payment.setUpdatedAt(Instant.now());
    payment.setUpdatedBy("SYSTEM");

    Payment saved = paymentRepository.save(payment);
    log.warn("Payment {} marked FAILED for orderId: {} | reason: {}",
            paymentId, payment.getOrderId(), reason);

    paymentFailedEvent.publishPaymentFailed(
            PaymentFailedEvent.builder()
                    .paymentId(saved.getPayment_id())
                    .orderId(saved.getOrderId())
                    .userId(saved.getUserId())
                    .amount(saved.getAmount())
                    .currency(saved.getCurrency())
                    .reason(reason)
                    .timestamp(Instant.now())
                    .build()
    );

    logAudit("PAYMENT_FAILED", saved.getUserId(),
            "Payment failed for orderId: " + saved.getOrderId()
    + " | reason: " + reason);

    return saved;
  }

  private List<PaymentCompletedEvent.InvoiceItemDto> buildInvoiceItems(Long orderId) {
    try {
      OrderDto order = orderServiceClient.getOrderById(orderId);
      return order.getItems().stream()
              .map(item -> PaymentCompletedEvent.InvoiceItemDto.builder()
                      .productName(item.getProductName())
                      .quantity(item.getQuantity())
                      .price(item.getUnitPrice())
                      .lineTotal(item.getUnitPrice()
                              .multiply(BigDecimal.valueOf(item.getQuantity())))
                      .build())
              .toList();
    } catch (Exception e) {
      log.warn("Could not fetch order items for orderId: {} - invoice will have not line items. {}",
              orderId, e.getMessage());
      return List.of();
    }
  }

  private void logAudit(String action, String userId, String details) {
    auditLogService.log(
            action,
            userId,
            "SYSTEM",
            details
    );
  }
}
