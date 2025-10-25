package com.phumlanidev.paymentservice.event.publisher;

import com.phumlanidev.commonevents.events.payment.PaymentInitiatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PublishPaymentInitiatedEvent extends BaseEventPublisher<PaymentInitiatedEvent>{

  private final KafkaTemplate<String, PaymentInitiatedEvent> kafkaTemplate;

  protected PublishPaymentInitiatedEvent(
          KafkaTemplate<String, PaymentInitiatedEvent> kafkaTemplate) {
    super(kafkaTemplate, "payment-initiated-dlq");
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishPaymentInitiated(PaymentInitiatedEvent event) {
    log.info("💳 Publishing PaymentInitiatedEvent for order ID: {}", event.getOrderId());
    processWithDlq(event.getOrderId().toString(),event, () -> {
      kafkaTemplate.send("payment.initiated", event);
      log.info("📤 Published PaymentInitiatedEvent: {}", event);
    });
  }
}
