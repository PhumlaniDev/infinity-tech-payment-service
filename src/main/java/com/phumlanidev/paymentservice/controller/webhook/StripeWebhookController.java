package com.phumlanidev.paymentservice.controller.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phumlanidev.paymentservice.exception.order.OrderNotFoundException;
import com.phumlanidev.paymentservice.repository.OrderRepository;
import com.phumlanidev.paymentservice.service.impl.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

  private final PaymentService paymentService;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final OrderRepository orderRepository;

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  @PostMapping("/webhook")
  public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
      log.info("✅ Received Stripe webhook payload");

      String secret = webhookSecret;

      Event event = Webhook.constructEvent(payload, sigHeader, secret);

      log.info("👉 Stripe event type: {}", event.getType());

      if ("checkout.session.completed".equals(event.getType())) {
        // Parse raw payload into JsonNode tree
        JsonNode rootNode = objectMapper.readTree(payload);
        JsonNode sessionNode = rootNode.path("data").path("object");
        log.debug("🔍 Stripe session metadata: {}", sessionNode.path("metadata").toPrettyString());

        String orderId = sessionNode.path("metadata").path("orderId").asText();
        log.info("🔍 Metadata orderId={}", orderId);
        if (orderId == null || orderId.isEmpty()) {
          log.warn("⚠️ Order ID missing in Stripe metadata");
          return ResponseEntity.badRequest().body("Order ID not found");
        }
        log.info("✅ Stripe checkout completed for orderId={}", orderId);
        paymentService.handlePaymentSuccess(Long.valueOf(orderId));
      }
      // Log all existing orders for debugging
      orderRepository.findAll().forEach(o -> log.info("Existing order: {}", o.getOrderId()));


      return ResponseEntity.ok("Webhook received");

    } catch (OrderNotFoundException ex) {
      log.error("❌ Order not found. Will retry on next webhook attempt");
      return ResponseEntity.status(500).body("Order not ready");

    } catch (SignatureVerificationException e) {
      log.error("❌ Invalid Stripe signature: {}", e.getMessage());
      return ResponseEntity.badRequest().body("Invalid signature");

    } catch (Exception e) {
      log.error("❌ Unexpected error in Stripe webhook handler: {}", e.getMessage(), e);
      return ResponseEntity.status(500).body("Webhook handling failed");
    }
  }
}