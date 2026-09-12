package com.phumlanidev.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {

  @NotNull(message= "Product name is required")
  private String productName;
  @NotNull(message = "Quantity is required")
  private Integer quantity;
  @NotNull(message = "Price is required")
  private BigDecimal unitPrice;
}
