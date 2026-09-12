package com.phumlanidev.paymentservice.webhook;

import com.phumlanidev.paymentservice.service.impl.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookHandler {

  private final PaymentService paymentService;

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  public void handleEvent(String payload, String sigHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
    } catch (SignatureVerificationException e) {
      log.warn("Invalid Stripe webhook signature: {}", e.getMessage());
      throw new RuntimeException("Invalid webhook signature", e);
    }

    StripeObject stripeObject = event.getDataObjectDeserializer()
            .getObject()
            .orElseThrow(() -> new RuntimeException("Could not deserialize Stripe event"));

    switch (event.getType()) {

      case "checkout.session.completed" -> {
        Session session = (Session) stripeObject;
        Map<String, String> metadata = session.getMetadata();

        Long paymentId    = Long.valueOf(metadata.get("paymentId"));
        String stripeId   = session.getId();         // Stripe session ID
        String userEmail  = metadata.get("userEmail");
        String custName   = metadata.get("customerName");

        paymentService.completePayment(
                paymentId,
                stripeId,    // stored as transactionId on Payment entity
                userEmail,   // for notification email
                custName     // for invoice PDF
        );
      }

      case "checkout.session.expired",
           "payment_intent.payment_failed" -> {
        Session session = (Session) stripeObject;
        Long paymentId = Long.valueOf(session.getMetadata().get("paymentId"));

        paymentService.markPaymentFailed(
                paymentId,
                "Stripe event: " + event.getType()  // ← reason now flows through
        );
      }

      default -> log.debug("Unhandled Stripe event type: {}", event.getType());
    }
  }
}
