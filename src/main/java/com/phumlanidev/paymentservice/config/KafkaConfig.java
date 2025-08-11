package com.phumlanidev.paymentservice.config;

import com.phumlanidev.commonevents.events.PaymentRequestEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

  private final static String bootstrapServers = "localhost:29092";

  @Bean
  public ConsumerFactory<String, PaymentRequestEvent> paymentRequestEventConsumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put("spring.deserializer.key.delegate.class", StringDeserializer.class);
    props.put("spring.deserializer.value.delegate.class", JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.phumlanidev.commonevents.events");
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Use type headers for deserialization
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.phumlanidev.commonevents.events.PaymentRequestEvent");
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestEvent> paymentRequestEventConsumerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, PaymentRequestEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(paymentRequestEventConsumerFactory());
    factory.setConcurrency(3); // Match your existing event concurrency
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    factory.setCommonErrorHandler(new DefaultErrorHandler());
    return factory;
  }
}
