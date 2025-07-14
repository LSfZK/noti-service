package lsfzk.notiservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // We configure the real deserializers here. The ErrorHandlingDeserializer will be added later.
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // Use String for raw JSON

        // --- THIS IS THE KEY ---
        // Use the ErrorHandlingDeserializer to wrap the real deserializers.
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Configure the REAL deserializers that the ErrorHandlingDeserializer will use.
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

        // Configure the JsonDeserializer itself.
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // Trust all packages for simplicity, or list them.
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Don't rely on producer's type headers.
        // -----------------------

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // --- THIS IS THE FIX ---
        // Create an error handler that will retry a failed delivery 9 times, waiting 1 second between each attempt.
        // This gives the Kafka broker plenty of time to finish starting up.
        // The 'false' means we are not treating deserialization exceptions as fatal.
//        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 9L));
//        errorHandler.setAckAfterHandle(false);

        // --- THIS IS THE FIX ---
        // Create an error handler that will log the problematic record's details.
        // It will retry twice (1 second apart) and then give up.
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    log.error("--- POISON PILL DETECTED ---");
                    log.error("Error processing Kafka message. The consumer will skip this record.");
                    log.error("Topic: {}", consumerRecord.topic());
                    log.error("Partition: {}", consumerRecord.partition());
                    log.error("Offset: {}", consumerRecord.offset());
                    log.error("Key: {}", consumerRecord.key());
                    log.error("Value: {}", consumerRecord.value());
                    log.error("Exception: {}", exception.getMessage());
                    log.error("--- END OF POISON PILL ---");
                },
                new FixedBackOff(1000L, 2L)
        );
        factory.setCommonErrorHandler(errorHandler);
        // -----------------------

        return factory;
    }
}
