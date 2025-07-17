package com.phumlanidev.paymentservice.utils;

import com.phumlanidev.paymentservice.client.NotificationClient;
import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceWrapper {

  private final NotificationClient notificationClient;

  @CircuitBreaker(name = "notificationService", fallbackMethod = "paymentFallback")
  @Retry(name = "notificationService")
  @TimeLimiter(name = "notificationService")
  public void sendPaymentConfirmationNotification(PaymentConfirmationRequestDto paymentConfirmationRequestDto){
    notificationClient.sendPaymentConfirmationNotification(paymentConfirmationRequestDto);
  }
}
