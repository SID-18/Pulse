package com.pulse.event.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfiguration {

    @Bean
    CommonErrorHandler kafkaErrorHandler(
        KafkaTemplate<Object, Object> kafkaTemplate,
        @Value("${pulse.kafka.consumer.retry-backoff-ms:1000}") long retryBackoffMs,
        @Value("${pulse.kafka.consumer.retry-attempts:2}") long retryAttempts
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(retryBackoffMs, retryAttempts));
    }
}
