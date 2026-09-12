package com.phumlanidev.paymentservice.helper;

import com.phumlanidev.paymentservice.dto.PaymentResponseDto;
import com.phumlanidev.paymentservice.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

  public PaymentResponseDto toResponse(Payment payment) {
    return PaymentResponseDto.builder()
            .paymentId(payment.getPayment_id())
            .orderId(payment.getOrderId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .paymentMethod(payment.getPaymentMethod())
            .status(payment.getPaymentStatus().name())
            .transactionId(payment.getTransactionId())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
  }
}