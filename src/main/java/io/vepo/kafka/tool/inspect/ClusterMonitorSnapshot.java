package io.vepo.kafka.tool.inspect;

import java.util.List;

public record ClusterMonitorSnapshot(ClusterSummary summary, List<ClusterBrokerInfo> brokers,
                                     List<ClusterTopicSummary> topics, List<ClusterPartitionSummary> partitions,
                                     List<BrokerLogDirSummary> logDirs, ReplicationStats replicationStats) {

}
