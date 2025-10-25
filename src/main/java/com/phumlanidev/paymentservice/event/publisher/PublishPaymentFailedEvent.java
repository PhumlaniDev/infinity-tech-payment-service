package com.phumlanidev.paymentservice.event.publisher;

import com.phumlanidev.commonevents.events.payment.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PublishPaymentFailedEvent extends BaseEventPublisher<PaymentFailedEvent>{

  private final KafkaTemplate<String, PaymentFailedEvent> kafkaTemplate;

  protected PublishPaymentFailedEvent(
          KafkaTemplate<String, PaymentFailedEvent> kafkaTemplate) {
    super(kafkaTemplate, "payment-failed-dlq");
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishPaymentFailed(PaymentFailedEvent event) {
    log.info("❌ Publishing PaymentFailedEvent for order ID: {}", event.getOrderId());
    processWithDlq(String.valueOf(event.getOrderId()),event, () -> {
      kafkaTemplate.send("payment.failed", event);
      log.info("📤 Published PaymentFailedEvent: {}", event);
    });
  }
}
