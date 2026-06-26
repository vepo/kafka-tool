package io.vepo.kafka.tool.inspect;

import java.util.Map;

import org.apache.kafka.clients.admin.TopicDescription;

public record ReplicationStats(int minReplicationFactor, int maxReplicationFactor, double averageReplicationFactor,
                               int topicsBelowBrokerCount) {

    public static ReplicationStats fromTopicDescriptions(Map<String, TopicDescription> descriptions, int brokerCount) {
        if (descriptions.isEmpty()) {
            return new ReplicationStats(0, 0, 0, 0);
        }
        int min = Integer.MAX_VALUE;
        int max = 0;
        int totalRf = 0;
        int topicsBelowBrokerCount = 0;
        for (TopicDescription description : descriptions.values()) {
            int rf = description.partitions().isEmpty() ? 0 : description.partitions().getFirst().replicas().size();
            min = Math.min(min, rf);
            max = Math.max(max, rf);
            totalRf += rf;
            if (rf < brokerCount) {
                topicsBelowBrokerCount++;
            }
        }
        double average = (double) totalRf / descriptions.size();
        return new ReplicationStats(min == Integer.MAX_VALUE ? 0 : min, max, average, topicsBelowBrokerCount);
    }

}
