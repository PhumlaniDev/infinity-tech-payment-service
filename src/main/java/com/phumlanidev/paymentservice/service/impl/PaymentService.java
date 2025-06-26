package com.phumlanidev.paymentservice.service.impl;

import com.phumlanidev.paymentservice.config.JwtAuthenticationConverter;
import com.phumlanidev.paymentservice.config.KeycloakTokenProvider;
import com.phumlanidev.paymentservice.dto.OrderDto;
import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final RestTemplate restTemplate;
  private final KeycloakTokenProvider keycloakTokenProvider;
  private final AuditLogServiceImpl auditLogService;
  private final HttpServletRequest request;
  private JwtAuthenticationConverter jwtAuthenticationConverter;

  @Value("${keycloak.resource}") // client ID
  private String clientId;

  @Value("${keycloak.credentials.secret}")
  private String clientSecret;

  @Transactional
  public void handlePaymentSuccess(String orderId) {
    try {
      String token = keycloakTokenProvider.getAccessToken(clientId, clientSecret);

      log.info("🔑 Token = {}", token.substring(7));

      // Notify the order service to mark the order as PAID
      String markPaidUrl = "http://localhost:9300/api/v1/order/mark-paid/"+ orderId;
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);
      HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
      log.info("Marking order as PAID for order ID: {}", orderId);
      restTemplate.exchange(markPaidUrl, HttpMethod.PUT, requestEntity, Void.class);
      log.info("✅ Order marked as PAID successfully for order ID: {}", orderId);

      // Notify the notification service
      String notifyUrl = "http://localhost:9500/api/v1/notifications/payment-confirmation";

      String getOrderDetailsUrl = "http://localhost:9300/api/v1/order/" + orderId;

      ResponseEntity<OrderDto> orderResponse = restTemplate.exchange(
              getOrderDetailsUrl,
              HttpMethod.GET,
              requestEntity,
              OrderDto.class
      );

      OrderDto order = orderResponse.getBody();

      PaymentConfirmationRequestDto confirmationDto = PaymentConfirmationRequestDto.builder()
              .orderId(orderId)
              .totalAmount(Objects.requireNonNull(order).getTotalPrice()) // Example amount, replace with actual
              .currency("USD") // Example currency, replace with actual
              .toEmail(order.getEmail()) // Example email, replace with actual
              .timestamp(Instant.now())
              .build();

      HttpEntity<PaymentConfirmationRequestDto> notifyRequest = new HttpEntity<>(confirmationDto, headers);
      restTemplate.exchange(notifyUrl, HttpMethod.POST, notifyRequest, Void.class);
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
