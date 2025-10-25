package com.phumlanidev.paymentservice.model;

import com.phumlanidev.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_tx", columnList = "transaction_id"),
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment extends BaseEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Long payment_id;
  @Column(name = "order_id", nullable = false)
  private Long orderId;
  @Column(name = "user_id", nullable = false)
  private String userId;
  @Column(name = "amount", nullable = false)
  private BigDecimal amount;
  @Column(name = "currency", nullable = false)
  private String currency;
  @Column(name = "payment_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus; // e.g., PENDING, COMPLETED, FAILED
  @Column(name = "payment_method", nullable = false)
  private String paymentMethod; // e.g., CREDIT_CARD, PAYPAL
  @Column(name = "transaction_id", unique = true)
  private String transactionId; // ID from payment gateway
}
