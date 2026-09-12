package com.phumlanidev.paymentservice.dto;

import com.phumlanidev.paymentservice.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PaymentConfirmationRequestDto {

  @NotNull(message = "Order ID is required")
  private Long orderId; // Unique order identifier

  @NotNull(message = "User ID is required")
  private String userId; // Unique user identifier

  @NotBlank(message = "Customer name is required")
  private String customerName;

  @NotBlank(message = "Email is required")
  @Email
  private String toEmail; // User's email address

  @NotNull(message = "Amount in cent is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
  private BigDecimal totalAmount; // in cents

  @NotBlank(message = "Currency is required")
  private String currency; // e.g., "USD", "EUR"

  @NotNull(message = "Payment method is required")
  private PaymentMethod paymentMethod; // e.g., CREDIT_CARD, PAYPAL'

  @NotEmpty(message = "Order must have at least one item")
  private List<OrderItemDto> items;


  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderItemDto {

    @NotNull(message= "Product name is required")
    private String productName;
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    @NotNull(message = "Price is required")
    private BigDecimal unitPrice;
  }
}


