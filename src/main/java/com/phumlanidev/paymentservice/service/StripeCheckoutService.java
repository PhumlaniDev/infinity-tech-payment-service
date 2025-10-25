package com.phumlanidev.paymentservice.service;

import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import com.phumlanidev.paymentservice.dto.PaymentResponseDto;
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
    // Set your secret key. Remember to switch to your live secret key in production!
    Stripe.apiKey = secretKey;
  }

  public PaymentResponseDto createCheckoutSession(PaymentConfirmationRequestDto req) {

    try {
      Payment pending = paymentService.createPendingPayment(
              req.getOrderId(),
              req.getUserId(),
              req.getTotalAmount(),
              req.getCurrency(),
              req.getPaymentMethod().name()
      );

      // Create a PaymentIntent with the order amount and currency
      SessionCreateParams.LineItem.PriceData.ProductData productData =
              SessionCreateParams.LineItem.PriceData.ProductData.builder()
                      .setName(req.getProductName())
                      .build();

      // Create new line item with the above product data and associated price
      SessionCreateParams.LineItem.PriceData priceData =
              SessionCreateParams.LineItem.PriceData.builder()
                      .setCurrency(req.getCurrency() != null ? req.getCurrency() : "USD")
                      .setUnitAmount(req.getTotalAmount().longValue()) // in cents
                      .setProductData(productData)
                      .build();

      // Create new line item with the above price data
      SessionCreateParams.LineItem lineItem =
              SessionCreateParams
                      .LineItem.builder()
                      .setQuantity(req.getQuantity())
                      .setPriceData(priceData)
                      .build();


      // Create new session with the line items
      SessionCreateParams params = SessionCreateParams.builder()
              .setMode(SessionCreateParams.Mode.PAYMENT)
              .setSuccessUrl(successUrl)
              .setCancelUrl(cancelUrl)
              .setCustomerEmail(req.getToEmail())
              .putMetadata("orderId", String.valueOf(req.getOrderId()))
              .putMetadata("paymentId", String.valueOf(pending.getPayment_id()))
              .addLineItem(lineItem)
              .build();

      // Create new session
      Session session = Session.create(params);

      assert session != null;
      return PaymentResponseDto
              .builder()
              .status("SUCCESS")
              .message("Payment session created ")
              .sessionId(session.getId())
              .sessionUrl(session.getUrl())
              .build();
    }
    catch (StripeException e) {
      return PaymentResponseDto
              .builder()
              .status("FAILED")
              .message("Failed to create payment session: " + e.getMessage())
              .build();
    }
  }


}



