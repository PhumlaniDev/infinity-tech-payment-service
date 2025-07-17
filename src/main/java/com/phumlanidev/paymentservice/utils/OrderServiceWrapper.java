package com.phumlanidev.paymentservice.utils;

import com.phumlanidev.paymentservice.client.OrderClient;
import com.phumlanidev.paymentservice.dto.OrderDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceWrapper {

  private static final Logger log = LoggerFactory.getLogger(OrderServiceWrapper.class);
  private final OrderClient orderClient;

  @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackOrder")
  @Retry(name = "orderService")
  @TimeLimiter(name = "orderService")
  public void markOrderAsPaid(String orderId) {
    orderClient.markOrderAsPaid(orderId);
  }

  @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackOrder")
  @Retry(name = "orderService")
  @TimeLimiter(name = "orderService")
  public OrderDto getOrderDetails(String orderId) {
    return orderClient.getOrderDetails(orderId);
  }

  public void fallbackOrder(OrderDto request, Throwable ex) {
    log.error("Notification failed: {}", ex.getMessage());
  }
}
