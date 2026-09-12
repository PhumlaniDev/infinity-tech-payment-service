package com.phumlanidev.paymentservice.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "User ID is required")
  private String userId;

  @NotBlank(message = "Email is required")
  private String userEmail;

  @NotBlank(message = "Customer name is required")
  private String customerName;

  @NotBlank(message = "Status is required")
  private String status;

  @NotNull(message = "Total price is required")
  private BigDecimal totalPrice;

  @NotEmpty(message = "Order items cannot be empty")
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
