package com.phumlanidev.paymentservice.client;

import com.phumlanidev.paymentservice.config.JwtAuthenticationConverter;
import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationClient {

  private final RestTemplate restTemplate;
  private final JwtAuthenticationConverter jwtAuthenticationConverter;
  private final HttpServletRequest request;

  public void sendPaymentConfirmation(PaymentConfirmationRequestDto dto) {
    try {
      String url = "http://localhost:9500/api/v1/notifications/payment-confirmation";
      restTemplate.postForEntity(url, dto, Void.class);
      log.info("✅ Notification sent for order ID: {}", dto.getOrderId());
    } catch (Exception e) {
      log.error("❌ Failed to send payment notification: {}", e.getMessage());
    }
  }
}
