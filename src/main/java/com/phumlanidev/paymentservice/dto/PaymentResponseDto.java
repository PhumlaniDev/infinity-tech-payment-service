package com.phumlanidev.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponseDto {

  @NotBlank(message = "Status is required")
  private String status;
  @NotBlank(message = "Message is required")
  private String message;
  @NotBlank(message = "Session ID is required")
  private String sessionId;
  @NotBlank(message = "Session URL is required")
  private String sessionUrl;
}
