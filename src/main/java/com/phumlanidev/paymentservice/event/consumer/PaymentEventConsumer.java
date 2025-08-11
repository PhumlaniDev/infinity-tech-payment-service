package com.phumlanidev.paymentservice.event.consumer;

import com.phumlanidev.commonevents.events.PaymentRequestEvent;
import com.phumlanidev.paymentservice.service.impl.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

  private final PaymentService paymentService;

  @Retryable(
          maxAttempts = 3,
          backoff = @Backoff(delay = 1000, multiplier = 2),
          retryFor = {RecoverableDataAccessException.class},
          noRetryFor = {IllegalAccessException.class}
  )
  @KafkaListener(
          topics = "payment.completed",
          groupId = "payment-group",
          containerFactory = "paymentRequestEventConsumerContainerFactory",
          errorHandler = "paymentCompletedEventErrorHandler"
  )
  public void handlePaymentRequest(ConsumerRecord<String, PaymentRequestEvent> record) {
    PaymentRequestEvent event = record.value();

    log.info("💳 Processing payment for order ID: {}", event.getOrderId());
    paymentService.handlePaymentSuccess(event.getOrderId());

  }
}
