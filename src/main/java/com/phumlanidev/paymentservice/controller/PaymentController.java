package com.phumlanidev.paymentservice.controller;

import com.phumlanidev.paymentservice.dto.ApiResponse;
import com.phumlanidev.paymentservice.dto.CheckoutResponseDto;
import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import com.phumlanidev.paymentservice.dto.PaymentResponseDto;
import com.phumlanidev.paymentservice.service.PaymentQueryService;
import com.phumlanidev.paymentservice.service.StripeCheckoutService;
import com.phumlanidev.paymentservice.webhook.StripeWebhookHandler;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final StripeCheckoutService stripeCheckoutService;
  private final StripeWebhookHandler stripeWebhookHandler;
  private final PaymentQueryService paymentQueryService;

  // ── Write endpoints ───────────────────────────────────────────────────────

  /**
   * Called by the frontend when the user proceeds to checkout.
   * Creates a Stripe hosted checkout session and returns the URL
   * to redirect the user to.
   *
   * Flow:
   * Frontend → POST /checkout → StripeCheckoutService
   *   → createPendingPayment()      saves PENDING to DB
   *   → Session.create(params)      creates Stripe session
   *   → returns { sessionUrl }      frontend redirects user
   */
  @PostMapping("/checkout")
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "Create a Stripe checkout session")
  public ResponseEntity<ApiResponse<CheckoutResponseDto>> createCheckoutSession(
          @RequestBody @Valid PaymentConfirmationRequestDto request) {

    log.info("Checkout session requested for orderId: {}", request.getOrderId());

    CheckoutResponseDto response = stripeCheckoutService.createCheckoutSession(request);

    if ("FAILED".equals(response.getStatus())) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body(ApiResponse.error(response.getMessage()));
    }

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * Stripe calls this endpoint asynchronously after a payment event.
   * MUST be public — no JWT, no @PreAuthorize.
   * Stripe signs the payload with a webhook secret — we verify that instead.
   *
   * Flow:
   * Stripe → POST /webhook → StripeWebhookHandler
   *   → verifies Stripe signature
   *   → checkout.session.completed → paymentService.completePayment()
   *   → checkout.session.expired   → paymentService.markPaymentFailed()
   *
   * NOTE: This endpoint must NOT be secured by Spring Security.
   *       Add "/api/v1/payments/webhook" to your security permit list.
   */
  @PostMapping("/webhook")
  @Operation(summary = "Stripe webhook receiver — do not call directly")
  public ResponseEntity<Void> handleStripeWebhook(
          @RequestBody String payload,
          @RequestHeader("Stripe-Signature") String sigHeader) {

    log.info("Stripe webhook received");
    stripeWebhookHandler.handleEvent(payload, sigHeader);
    return ResponseEntity.ok().build();
  }

  // ── Read endpoints ────────────────────────────────────────────────────────

  /**
   * Frontend polls this after Stripe redirects back to successUrl
   * to confirm the payment status before showing a confirmation page.
   */
  @GetMapping("/{paymentId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Get payment by ID")
  public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentById(
          @PathVariable Long paymentId) {

    return ResponseEntity.ok(ApiResponse.success(
            paymentQueryService.getPaymentById(paymentId)));
  }

  /**
   * Used by order-service or frontend to check payment status for an order.
   */
  @GetMapping("/order/{orderId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Get payment status for an order")
  public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByOrderId(
          @PathVariable Long orderId) {

    return ResponseEntity.ok(ApiResponse.success(
            paymentQueryService.getPaymentByOrderId(orderId)));
  }

  /**
   * Used by admin dashboard or user payment history page.
   */
  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
  @Operation(summary = "Get all payments for a user")
  public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPaymentsByUser(
          @PathVariable String userId) {

    return ResponseEntity.ok(ApiResponse.success(
            paymentQueryService.getPaymentsByUserId(userId)));
  }


}

