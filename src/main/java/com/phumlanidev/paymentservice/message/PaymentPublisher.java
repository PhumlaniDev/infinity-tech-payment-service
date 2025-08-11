package com.phumlanidev.paymentservice.message;

import com.phumlanidev.commonevents.events.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class PaymentPublisher{

  private final StreamBridge streamBridge;

  public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
    log.info("📤 Emitting PaymentCompletedEvent for order: {}", event.getOrderId());
    streamBridge.send("paymentCompleted-out-0", event);
  }
}