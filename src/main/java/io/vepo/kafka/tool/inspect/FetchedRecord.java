package io.vepo.kafka.tool.inspect;

public record FetchedRecord(MessageMetadata metadata, KafkaMessage message) {

}
