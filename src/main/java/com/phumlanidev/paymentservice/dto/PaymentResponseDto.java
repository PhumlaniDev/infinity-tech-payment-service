package com.phumlanidev.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponseDto {

//  @NotBlank(message = "Status is required")
//  private String status;
//  @NotBlank(message = "Message is required")
//  private String message;
//  @NotBlank(message = "Session ID is required")
//  private String sessionId;
//  @NotBlank(message = "Session URL is required")
//  private String sessionUrl;

  private Long paymentId;
  private Long orderId;
  private String userId;
  private BigDecimal amount;
  private String currency;
  private String paymentMethod;
  private String status;
  private String transactionId;
  private Instant createdAt;
  private Instant updatedAt;
}
