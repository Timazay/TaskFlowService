package by.timofey.testtask.config;

import by.timofey.testtask.dto.CreateTaskEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaPropertyConfig config;

    @Bean
    public ConsumerFactory<String, CreateTaskEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                config.getBootstrapServers());
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                config.getConsumer().keyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false);
        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                config.getConsumer().autoOffsetReset());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumer().groupId());
        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "by.timofey.testtask.*");
        props.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false);

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 52428800);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CreateTaskEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreateTaskEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CreateTaskEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.MANUAL
        );
        factory.setBatchListener(true);

        return factory;
    }
}


