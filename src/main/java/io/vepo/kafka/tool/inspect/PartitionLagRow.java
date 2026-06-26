package io.vepo.kafka.tool.inspect;

public record PartitionLagRow(String groupId, String topic, int partition, long committedOffset, long endOffset,
                              long lag) {

}
