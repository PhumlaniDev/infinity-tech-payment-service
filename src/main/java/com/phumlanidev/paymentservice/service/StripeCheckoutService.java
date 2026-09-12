package com.phumlanidev.paymentservice.service;

import com.phumlanidev.paymentservice.dto.CheckoutResponseDto;
import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import com.phumlanidev.paymentservice.model.Payment;
import com.phumlanidev.paymentservice.service.impl.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutService {

  @Value("${stripe.success-url}")
  private String successUrl;

  @Value("${stripe.cancel-url}")
  private String cancelUrl;

  @Value("${stripe.secret-key}")
  private String secretKey;

  private final PaymentService paymentService;

  @PostConstruct
  public void init() {
    Stripe.apiKey = secretKey;
    log.info("Stripe API initialized");
  }

  public CheckoutResponseDto createCheckoutSession(   // ← was PaymentResponseDto
                                                      PaymentConfirmationRequestDto req) {
    try {
      Payment pending = paymentService.createPendingPayment(
              req.getOrderId(),
              req.getUserId(),
              req.getTotalAmount(),
              req.getCurrency(),
              req.getPaymentMethod().name()
      );

      List<SessionCreateParams.LineItem> lineItems =
              buildLineItems(req.getItems(), req.getCurrency());

      SessionCreateParams params = SessionCreateParams.builder()
              .setMode(SessionCreateParams.Mode.PAYMENT)
              .setSuccessUrl(successUrl)
              .setCancelUrl(cancelUrl)
              .setCustomerEmail(req.getToEmail())
              .putMetadata("orderId",      String.valueOf(req.getOrderId()))
              .putMetadata("paymentId",    String.valueOf(pending.getPayment_id()))
              .putMetadata("userEmail",    req.getToEmail())
              .putMetadata("customerName", req.getCustomerName())
              .addAllLineItem(lineItems)
              .build();

      Session session = Session.create(params);

      if (session == null || session.getUrl() == null) {
        log.error("Stripe returned null session for orderId: {}",
                req.getOrderId());
        throw new RuntimeException("Stripe session creation returned null");
      }

      log.info("Checkout session created for orderId: {} | sessionId: {}",
              req.getOrderId(), session.getId());

      return CheckoutResponseDto.builder()  // ← was PaymentResponseDto
              .status("SUCCESS")
              .message("Payment session created")
              .sessionId(session.getId())
              .sessionUrl(session.getUrl())
              .build();

    } catch (StripeException e) {
      log.error("Stripe error for orderId: {} | {}",
              req.getOrderId(), e.getMessage());

      return CheckoutResponseDto.builder()  // ← was PaymentResponseDto
              .status("FAILED")
              .message("Failed to create payment session: " + e.getMessage())
              .build();
    }
  }

  // ── private helpers ───────────────────────────────────────────────────────

  private List<SessionCreateParams.LineItem> buildLineItems(
          List<PaymentConfirmationRequestDto.OrderItemDto> items,
          String currency) {

    return items.stream()
            .map(item -> {
              long unitAmountInCents = item.getUnitPrice()
                      .multiply(BigDecimal.valueOf(100))
                      .longValue();

              SessionCreateParams.LineItem.PriceData.ProductData productData =
                      SessionCreateParams.LineItem.PriceData.ProductData.builder()
                              .setName(item.getProductName())
                              .build();

              SessionCreateParams.LineItem.PriceData priceData =
                      SessionCreateParams.LineItem.PriceData.builder()
                              .setCurrency(currency != null
                                      ? currency.toLowerCase() : "usd")
                              .setUnitAmount(unitAmountInCents)
                              .setProductData(productData)
                              .build();

              return SessionCreateParams.LineItem.builder()
                      .setQuantity((long) item.getQuantity())
                      .setPriceData(priceData)
                      .build();
            })
            .toList();
  }
}



