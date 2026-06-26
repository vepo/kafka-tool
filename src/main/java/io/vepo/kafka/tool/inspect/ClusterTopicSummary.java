package io.vepo.kafka.tool.inspect;

public record ClusterTopicSummary(String name, int partitions, int replicationFactor) {

}
