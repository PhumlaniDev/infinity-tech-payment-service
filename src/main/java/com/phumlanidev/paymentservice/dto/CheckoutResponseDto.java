package com.phumlanidev.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDto {

  private String status;      // "SUCCESS" or "FAILED"
  private String message;
  private String sessionId;   // Stripe session ID — used to resume session if needed
  private String sessionUrl;  // Stripe hosted checkout URL — frontend redirects here
}