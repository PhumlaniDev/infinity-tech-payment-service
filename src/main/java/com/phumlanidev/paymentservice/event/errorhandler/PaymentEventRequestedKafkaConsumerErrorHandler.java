package com.phumlanidev.paymentservice.event.errorhandler;


import com.phumlanidev.commonevents.events.payment.PaymentRequestEvent;
import com.phumlanidev.paymentservice.event.dlq.PaymentRequestedEventDlqPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("paymentCompletedEventErrorHandler")
@RequiredArgsConstructor
@Slf4j
public class PaymentEventRequestedKafkaConsumerErrorHandler implements KafkaListenerErrorHandler {

  private final PaymentRequestedEventDlqPublisher dlqPublisher;

  @Override
  public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
    log.error("❗ Kafka error handler caught: {}", exception.getMessage());

    try {
      ConsumerRecord<String, PaymentRequestEvent> record = (ConsumerRecord<String, PaymentRequestEvent>) message.getPayload();
      log.warn("❌ DLQ fallback for message: {}", record.value());
      dlqPublisher.publishToDlq(record.key(), record.value(), exception);
    } catch (Exception e) {
      log.error("Failed to handle PaymentRequestedEvent error: {}", e.getMessage(), e);
    }
    return null;
  }
}
