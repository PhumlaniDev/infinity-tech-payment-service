package com.phumlanidev.paymentservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phumlanidev.paymentservice.service.impl.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

  private final PaymentService paymentService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  @PostMapping("/webhook")
  public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
      log.info("✅ Received Stripe webhook payload");
      Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
      if ("checkout.session.completed".equals(event.getType())) {
        // Parse raw payload into JsonNode tree
        JsonNode rootNode = objectMapper.readTree(payload);
        JsonNode sessionNode = rootNode.path("data").path("object");

//        log.info("Session data: {}", sessionNode.path("billing_details").toString());

        Long paymentId = sessionNode.path("metadata").path("paymentId").asLong();
        String transactionId = sessionNode.path("payment_intent").asText();
        log.info("💰 Payment intent (transaction ID): {}", transactionId);

        if (paymentId == null || paymentId < 0){
          log.warn("⚠️ Missing or invalid paymentId in metadata");
          return ResponseEntity.badRequest().body("Invalid payment ID in metadata");
        }

        String userEmail = sessionNode.path("customer_details").path("email").asText();
        log.info("📧 Customer email: {}", userEmail);

        if (userEmail == null || userEmail.isEmpty()) {
          log.warn("⚠️ Missing customer email in session data");
          return ResponseEntity.badRequest().body("Missing customer email");
        }
        paymentService.handlePaymentSuccess(paymentId, transactionId, userEmail);
      }
      return ResponseEntity.ok("Webhook received");
    } catch (SignatureVerificationException e) {
      log.warn("❌ Invalid Stripe webhook signature: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    } catch (Exception e) {
      log.error("❌ Unexpected error in Stripe webhook handler: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook handling failed");
    }
  }
}