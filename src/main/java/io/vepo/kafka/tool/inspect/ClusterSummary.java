package io.vepo.kafka.tool.inspect;

import java.util.Map;

public record ClusterSummary(String clusterId, int controllerId, int brokerCount, int userTopicCount,
                             int internalTopicCount, int totalPartitions, int underReplicatedCount, int offlineCount,
                             Map<String, Integer> consumerGroupsByState, String schemaRegistryStatus) {

    public int consumerGroupCount() {
        return consumerGroupsByState.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean healthy() {
        return underReplicatedCount == 0 && offlineCount == 0;
    }

    public String healthSummary() {
        if (healthy()) {
            return "Cluster healthy — %d broker(s), %d partition(s).".formatted(brokerCount, totalPartitions);
        }
        return "Issues detected — %d under-replicated, %d offline partition(s)."
                                                                                .formatted(underReplicatedCount, offlineCount);
    }

}
