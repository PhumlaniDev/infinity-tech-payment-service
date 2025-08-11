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

  @CircuitBreaker(name = "orderService", fallbackMethod = "markOrderAsPaidFallback")
  @Retry(name = "orderService")
  @TimeLimiter(name = "orderService")
  public void markOrderAsPaid(Long orderId) {
    orderClient.markOrderAsPaid(orderId);
  }

  @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderDetailsFallback")
  @Retry(name = "orderService")
  @TimeLimiter(name = "orderService")
  public OrderDto getOrderDetails(Long orderId) {
    return orderClient.getOrderDetails(orderId);
  }

  public void markOrderAsPaidFallback(Throwable ex) {
    log.error("Marking order as paid failed: {}", ex.getMessage());
  }

  public void getOrderDetailsFallback(Throwable ex) {
    log.error("Getting order details failed: {}", ex.getMessage());
  }
}
