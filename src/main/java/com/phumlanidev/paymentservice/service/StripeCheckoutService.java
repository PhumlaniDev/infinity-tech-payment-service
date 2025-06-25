package com.phumlanidev.paymentservice.service;

import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import com.phumlanidev.paymentservice.dto.PaymentResponseDto;
import com.phumlanidev.paymentservice.model.Order;
import com.phumlanidev.paymentservice.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeCheckoutService {

  @Value("${stripe.success-url}")
  private String successUrl;

  @Value("${stripe.cancel-url}")
  private String cancelUrl;

  @Value("${stripe.secret-key}")
  private String secretKey;

  private final OrderRepository orderRepository;

  @PostConstruct
  public void init() {
    // Set your secret key. Remember to switch to your live secret key in production!
    Stripe.apiKey = secretKey;
  }

  public PaymentResponseDto createCheckoutSession(PaymentConfirmationRequestDto productRequest) {

    try {
      // Create a PaymentIntent with the order amount and currency
      SessionCreateParams.LineItem.PriceData.ProductData productData =
              SessionCreateParams.LineItem.PriceData.ProductData.builder()
                      .setName(productRequest.getProductName())
                      .build();

      // Create new line item with the above product data and associated price
      SessionCreateParams.LineItem.PriceData priceData =
              SessionCreateParams.LineItem.PriceData.builder()
                      .setCurrency(productRequest.getCurrency() != null ? productRequest.getCurrency() : "USD")
                      .setUnitAmount(productRequest.getTotalAmount().longValue()) // in cents
                      .setProductData(productData)
                      .build();

      // Create new line item with the above price data
      SessionCreateParams.LineItem lineItem =
              SessionCreateParams
                      .LineItem.builder()
                      .setQuantity(productRequest.getQuantity())
                      .setPriceData(priceData)
                      .build();

      Optional<Order> savedOrder = orderRepository.findById(Long.valueOf(productRequest.getOrderId()));

      // Create new session with the line items
      SessionCreateParams params = SessionCreateParams.builder()
              .setMode(SessionCreateParams.Mode.PAYMENT)
              .setSuccessUrl(successUrl)
              .setCancelUrl(cancelUrl)
              .setCustomerEmail(productRequest.getToEmail())
              .putMetadata("orderId", String.valueOf(savedOrder.map(Order::getOrderId)
                      .orElse(Long.valueOf(productRequest.getOrderId()))))
              .putMetadata("email", productRequest.getToEmail())
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



