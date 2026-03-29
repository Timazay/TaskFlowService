package by.timofey.testtask.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaPropertyConfig propertyConfig;

    @Bean
    public NewTopic taskAssignUserTopic() {
        return TopicBuilder.name(propertyConfig.getTopics().taskAssignUser())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic taskCreateRequestTopic() {
        return TopicBuilder.name(propertyConfig.getTopics().taskCreateEvent())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
