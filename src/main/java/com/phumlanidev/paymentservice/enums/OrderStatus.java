package com.phumlanidev.paymentservice.enums;

/**
 * Comment: this is the placeholder for documentation.
 */
public enum OrderStatus {
  PENDING,       // Order received but not yet processed
  PROCESSING,    // Order is being prepared
  SHIPPED,       // Order has been dispatched
  DELIVERED,     // Order has been delivered to the customer
  CANCELLED,     // Order has been cancelled
  ON_HOLD,       // Order is temporarily paused
  COMPLETED,     // Order has been fully processed and fulfilled
  RETURNED,       // Order has been returned by the customer
  PLACED,         // Order has been placed by the customer
  PAID           // Order has been paid for
}
