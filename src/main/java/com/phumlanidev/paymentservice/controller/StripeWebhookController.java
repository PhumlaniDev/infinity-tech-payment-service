package com.phumlanidev.paymentservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            log.info("✅ Received Stripe webhook payload");

            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {

                JsonNode rootNode = objectMapper.readTree(payload);
                JsonNode sessionNode = rootNode.path("data").path("object");
                JsonNode metaDataNode = sessionNode.path("metadata");

                log.info("🧾 Metadata: {}", metaDataNode.toPrettyString());

                if (!metaDataNode.has("paymentId") ||
                        metaDataNode.get("paymentId").asText().isBlank()) {

                    log.warn("⚠️ Missing paymentId in metadata");
                    return ResponseEntity.ok("Webhook received");
                }

                Long paymentId =
                        Long.parseLong(metaDataNode.get("paymentId").asText());

                String transactionId =
                        sessionNode.path("payment_intent").asText();

                String userEmail =
                        sessionNode.path("customer_details").path("email").asText();

                if (userEmail == null || userEmail.isBlank()) {
                    log.warn("⚠️ Missing customer email");
                    return ResponseEntity.ok("Webhook received");
                }

                paymentService.handlePaymentSuccess(
                        paymentId, transactionId, userEmail
                );
            }

        } catch (SignatureVerificationException e) {
            log.warn("❌ Invalid Stripe signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        } catch (Exception e) {
            log.error("❌ Error processing webhook", e);
            // swallow error
        }

        return ResponseEntity.ok("Webhook received");
    }
}