package by.timofey.testtask.config.properties;

public record ConsumerProperty(String keyDeserializer,
                               String valueDeserializer,
                               String groupId,
                               String autoOffsetReset) {
}
