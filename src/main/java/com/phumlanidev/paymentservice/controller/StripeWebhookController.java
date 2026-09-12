package com.phumlanidev.paymentservice.controller;

import com.phumlanidev.paymentservice.webhook.StripeWebhookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

  private final StripeWebhookHandler webhookHandler;

  @PostMapping("/stripe")
  public ResponseEntity<Void> handleStripeWebhook(
          @RequestBody String payload,
          @RequestHeader("Stripe-Signature") String sigHeader) {

    log.info("Stripe webhook event received");
    webhookHandler.handleEvent(payload, sigHeader);
    return ResponseEntity.ok().build();
  }
}