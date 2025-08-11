package com.phumlanidev.paymentservice.client;

import com.phumlanidev.paymentservice.config.AuthFeingConfig;
import com.phumlanidev.paymentservice.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(
        name = "order-client",
        configuration = AuthFeingConfig.class
)
public interface OrderClient {

  @PutMapping("/api/v1/order/mark-paid/{orderId}")
  void markOrderAsPaid(Long orderId);

  @GetMapping("/api/v1/order/{orderId}")
  OrderDto getOrderDetails(Long orderId);
}
