package io.vepo.kafka.tool.inspect;

public record ReplicationStats(int minReplicationFactor, int maxReplicationFactor, double averageReplicationFactor,
                               int topicsBelowBrokerCount) {

}
