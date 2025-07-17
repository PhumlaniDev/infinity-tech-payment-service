package com.phumlanidev.paymentservice.service.impl;

import com.phumlanidev.paymentservice.config.JwtAuthenticationConverter;
import com.phumlanidev.paymentservice.dto.OrderDto;
import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import com.phumlanidev.paymentservice.utils.NotificationServiceWrapper;
import com.phumlanidev.paymentservice.utils.OrderServiceWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final AuditLogServiceImpl auditLogService;
  private final HttpServletRequest request;
  private final OrderServiceWrapper orderServiceWrapper;
  private final NotificationServiceWrapper notificationServiceWrapper;
  private final JwtAuthenticationConverter jwtAuthenticationConverter;

  @Transactional
  public void handlePaymentSuccess(String orderId) {
    try {
      // Notify the order service to mark the order as PAID
      log.info("Marking order as PAID for order ID: {}", orderId);
      orderServiceWrapper.markOrderAsPaid(orderId);
      log.info("✅ Order marked as PAID successfully for order ID: {}", orderId);

      OrderDto orderResponse = orderServiceWrapper.getOrderDetails(orderId);


      PaymentConfirmationRequestDto confirmationDto = PaymentConfirmationRequestDto.builder()
              .orderId(orderId)
              .totalAmount(Objects.requireNonNull(orderResponse).getTotalPrice()) // Example amount, replace with actual
              .currency("USD") // Example currency, replace with actual
              .toEmail(orderResponse.getEmail()) // Example email, replace with actual
              .timestamp(Instant.now())
              .build();

      notificationServiceWrapper.sendPaymentConfirmationNotification(confirmationDto);
      log.info("✅ Workflow complete: order marked as PAID and user notified");
      logAudit();

    } catch (Exception e) {
      log.error("❌ Failed to notify order-service to mark order as PAID: {}", e.getMessage());
    }
  }

  private void logAudit() {
    String clientIp = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";
    Jwt jwt = jwtAuthenticationConverter.getJwt();
    String userId = jwtAuthenticationConverter.extractUserId(jwt);


    auditLogService.log(
            "PAYMENT_SUCCESS",
            userId,
            username,
            clientIp,
            "Payment successful for"
    );
  }
}
