package com.phumlanidev.paymentservice.enums;

public enum PaymentStatus {

  PENDING,    // Payment is initiated but not yet completed
  COMPLETED,  // Payment has been successfully processed
  FAILED,     // Payment processing failed
  REFUNDED,   // Payment has been refunded to the customer
  CANCELLED   // Payment was cancelled before completion
}
