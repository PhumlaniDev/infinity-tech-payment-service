package com.phumlanidev.paymentservice.event.publisher;


import com.phumlanidev.commonevents.events.payment.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PublisherPaymentCompletedEvent extends BaseEventPublisher<PaymentCompletedEvent>{

  private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

  protected PublisherPaymentCompletedEvent(
          KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate) {
    super(kafkaTemplate, "payment-completed-dlq");
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishPaymentCompleted(PaymentCompletedEvent event) {
    log.info("✅ Publishing PaymentCompletedEvent for order ID: {}", event.getOrderId());
    processWithDlq(event.getOrderId().toString(),event, () -> {
      kafkaTemplate.send("payment.completed", event);
      log.info("📤 Published PaymentCompletedEvent: {}", event);
    });
  }
}
