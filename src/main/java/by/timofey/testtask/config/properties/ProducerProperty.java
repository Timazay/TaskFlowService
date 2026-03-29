package by.timofey.testtask.config.properties;

public record ProducerProperty(String keySerializer, String valueSerializer, String acks, String transactionIdPrefix) {
}
