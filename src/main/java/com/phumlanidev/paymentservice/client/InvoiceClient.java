package com.phumlanidev.paymentservice.client;

import com.phumlanidev.paymentservice.config.AuthFeingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "invoice-service",
        configuration = AuthFeingConfig.class
)
public interface InvoiceClient {

  @GetMapping("/api/v1/invoice/{orderId}")
  byte[] getInvoice(@PathVariable String orderId);
}
