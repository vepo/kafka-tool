package io.vepo.kafka.tool.inspect;

public record MessageMetadata(int partition, long offset, long timestamp) {

}
