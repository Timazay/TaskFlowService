package by.timofey.testtask.config;

import by.timofey.testtask.config.properties.ConsumerProperty;
import by.timofey.testtask.config.properties.ProducerProperty;
import by.timofey.testtask.config.properties.TopicProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaPropertyConfig {
    private final String bootstrapServers;
    private final ProducerProperty producer;
    private final TopicProperty topics;
    private final ConsumerProperty consumer;
}