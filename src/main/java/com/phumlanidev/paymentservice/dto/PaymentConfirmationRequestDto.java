package com.phumlanidev.paymentservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentConfirmationRequestDto {

  @NotBlank(message = "Product name is required")
  private String productName;
  @NotNull(message = "Amount in cent is required")
  @Min(50)
  private BigDecimal totalAmount; // in cents
  @NotBlank(message = "Currency is required")
  private String currency; // e.g., "USD", "EUR"
  @NotNull(message = "Quantity is required")
  private Long quantity;
  @NotBlank
  @Email
  private String toEmail; // User's email address
  @NotBlank(message = "Order ID is required")
  private Long orderId; // Unique order identifier
  @NotNull
  private Instant timestamp; // Timestamp of the payment confirmation request
}
