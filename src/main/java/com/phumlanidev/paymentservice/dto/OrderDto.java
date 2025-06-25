package com.phumlanidev.paymentservice.dto;

import com.phumlanidev.paymentservice.model.OrderItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDto {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Total price is required")
  private BigDecimal totalPrice;

  @NotEmpty(message = "Order items cannot be empty")
  private List<OrderItem> items;

  @NotNull(message = "Placed at is required")
  private LocalDateTime placedAt;

  @NotNull(message = "User ID is required")
  private String userId;

  @NotBlank(message = "Email is required")
  private String email;
}
