package com.phumlanidev.paymentservice.client;

import com.phumlanidev.paymentservice.config.AuthFeingConfig;
import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        configuration = AuthFeingConfig.class
)
public interface NotificationClient {

  @PostMapping("/api/v1/notifications/payment-confirmation")
  void sendPaymentConfirmationNotification(@RequestBody PaymentConfirmationRequestDto paymentConfirmationRequestDto);
}
