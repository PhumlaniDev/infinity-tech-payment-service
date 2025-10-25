package com.phumlanidev.paymentservice.controller;

import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import com.phumlanidev.paymentservice.dto.PaymentResponseDto;
import com.phumlanidev.paymentservice.model.Payment;
import com.phumlanidev.paymentservice.service.StripeCheckoutService;
import com.phumlanidev.paymentservice.service.impl.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final StripeCheckoutService stripeService;
  private final PaymentService paymentService;

  @PostMapping("/checkout")
  public ResponseEntity<PaymentResponseDto> createCheckoutSession(@Valid @RequestBody PaymentConfirmationRequestDto request) {
    PaymentResponseDto productRequest = stripeService.createCheckoutSession(request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(productRequest);
  }

  @PostMapping("/confirm/{transactionId}")
  public ResponseEntity<Payment> confirmPayment(@PathVariable String transactionId, Long paymentId) {
    Payment payment = paymentService.markPaymentAsCompleted(paymentId, transactionId);
    return ResponseEntity.ok(payment);
  }

  @PostMapping("/fail/{transactionId}")
  public ResponseEntity<Payment> failedPayment(@PathVariable String transactionId, Long paymentId) {
    Payment payment = paymentService.markPaymentFailed(paymentId, transactionId);
    return ResponseEntity.ok(payment);
  }
}

