package com.phumlanidev.paymentservice.clinet;

import com.phumlanidev.paymentservice.config.AuthFeignConfig;
import com.phumlanidev.paymentservice.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "order-service",
//        url = "${services.order-service.url}",
        path = "/api/v1/order",
        configuration = AuthFeignConfig.class
)
public interface OrderServiceClient {

  @GetMapping("/{orderId}")
  OrderDto getOrderById(@PathVariable("orderId") Long orderId);
}
