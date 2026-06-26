package io.vepo.kafka.tool.inspect;

public record TopicPartitionInfo(int partition, long beginningOffset, long endOffset) {

}
