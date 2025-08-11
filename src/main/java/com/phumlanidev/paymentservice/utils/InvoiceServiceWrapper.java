package com.phumlanidev.paymentservice.utils;

import com.phumlanidev.paymentservice.client.InvoiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
public class InvoiceServiceWrapper {

  private final InvoiceClient invoiceClient;

  @CircuitBreaker(name = "notificationService", fallbackMethod = "paymentFallback")
  @Retry(name = "notificationService")
  @TimeLimiter(name = "notificationService")
  public byte[] getInvoice(@PathVariable String orderId) {
    return invoiceClient.getInvoice(orderId);
  }
}
