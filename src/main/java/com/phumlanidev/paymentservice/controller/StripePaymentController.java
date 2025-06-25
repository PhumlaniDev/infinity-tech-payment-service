package com.phumlanidev.paymentservice.controller;

import com.phumlanidev.paymentservice.dto.PaymentConfirmationRequestDto;
import com.phumlanidev.paymentservice.dto.PaymentResponseDto;
import com.phumlanidev.paymentservice.service.StripeCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class StripePaymentController {

  private final StripeCheckoutService stripeService;

  @PostMapping("/checkout")
  public ResponseEntity<PaymentResponseDto> createCheckoutSession(@Valid @RequestBody PaymentConfirmationRequestDto request) {
    PaymentResponseDto productRequest = stripeService.createCheckoutSession(request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(productRequest);
  }
}

