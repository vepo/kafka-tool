package io.vepo.kafka.tool.inspect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeLogDirsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClusterMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterMonitorService.class);
    private static final int DESCRIBE_TOPICS_BATCH_SIZE = 50;

    public static ClusterMonitorService create() {
        return new ClusterMonitorService();
    }

    private ClusterMonitorService() {}

    private ReplicationStats computeReplicationStats(Map<String, TopicDescription> descriptions, int brokerCount) {
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

    public List<BrokerConfigEntry> describeBrokerConfig(AdminClient client, int brokerId) throws Exception {
        var resource = new ConfigResource(ConfigResource.Type.BROKER, String.valueOf(brokerId));
        Config config = client.describeConfigs(List.of(resource)).all().get().get(resource);
        return config.entries().stream()
                     .sorted(Comparator.comparing(ConfigEntry::name))
                     .map(entry -> new BrokerConfigEntry(entry.name(), entry.value(), entry.source().toString(),
                                                         entry.isSensitive()))
                     .collect(Collectors.toList());
    }

    private List<BrokerLogDirSummary> describeLogDirs(AdminClient client, Collection<Node> nodes) {
        if (nodes.isEmpty()) {
            return List.of();
        }
        try {
            var brokerIds = nodes.stream().map(Node::id).collect(Collectors.toList());
            DescribeLogDirsResult result = client.describeLogDirs(brokerIds);
            Map<Integer, Map<String, org.apache.kafka.clients.admin.LogDirDescription>> descriptions = result.allDescriptions()
                                                                                                             .get();
            List<BrokerLogDirSummary> summaries = new ArrayList<>();
            for (var brokerEntry : descriptions.entrySet()) {
                int brokerId = brokerEntry.getKey();
                for (var logDirEntry : brokerEntry.getValue().entrySet()) {
                    var logDir = logDirEntry.getKey();
                    var description = logDirEntry.getValue();
                    if (description.error() != null) {
                        summaries.add(new BrokerLogDirSummary(brokerId, logDir, -1, description.error().getMessage()));
                    } else {
                        long totalBytes = description.replicaInfos().values().stream().mapToLong(info -> info.size()).sum();
                        summaries.add(new BrokerLogDirSummary(brokerId, logDir, totalBytes, null));
                    }
                }
            }
            summaries.sort(Comparator.comparingInt(BrokerLogDirSummary::brokerId).thenComparing(BrokerLogDirSummary::logDir));
            return summaries;
        } catch (Exception e) {
            logger.warn("Could not describe broker log directories: {}", e.getMessage());
            return List.of();
        }
    }

    private Map<String, TopicDescription> describeTopicsInBatches(AdminClient client, List<String> topicNames) {
        var descriptions = new HashMap<String, TopicDescription>();
        for (int offset = 0; offset < topicNames.size(); offset += DESCRIBE_TOPICS_BATCH_SIZE) {
            var batch = topicNames.subList(offset, Math.min(offset + DESCRIBE_TOPICS_BATCH_SIZE, topicNames.size()));
            try {
                descriptions.putAll(client.describeTopics(batch).allTopicNames().get());
            } catch (Exception e) {
                logger.warn("Could not describe topics batch starting at {}: {}", offset, e.getMessage());
            }
        }
        return descriptions;
    }

    private Map<TopicPartition, Long> fetchLastOffsets(AdminClient client,
                                                       Map<String, TopicDescription> descriptions) {
        var specs = new HashMap<TopicPartition, OffsetSpec>();
        for (var description : descriptions.values()) {
            for (var partition : description.partitions()) {
                specs.put(new TopicPartition(description.name(), partition.partition()), OffsetSpec.latest());
            }
        }
        if (specs.isEmpty()) {
            return Map.of();
        }
        try {
            var offsets = client.listOffsets(specs).all().get();
            var lastOffsets = new HashMap<TopicPartition, Long>();
            for (var entry : offsets.entrySet()) {
                lastOffsets.put(entry.getKey(), entry.getValue().offset());
            }
            return lastOffsets;
        } catch (Exception e) {
            logger.warn("Could not list partition last offsets: {}", e.getMessage());
            return Map.of();
        }
    }

    public ClusterMonitorSnapshot loadSnapshot(AdminClient client, String schemaRegistryUrl) throws Exception {
        var clusterResult = client.describeCluster();
        var clusterId = clusterResult.clusterId().get();
        var controller = clusterResult.controller().get();
        var controllerId = controller.id();
        var nodes = clusterResult.nodes().get();

        var brokers = nodes.stream()
                           .map(node -> new ClusterBrokerInfo(node.id(), node.host(), node.port(), node.rack(),
                                                              node.id() == controllerId ? "Controller" : "Broker"))
                           .sorted(Comparator.comparingInt(ClusterBrokerInfo::brokerId))
                           .collect(Collectors.toList());

        var topicListings = client.listTopics().listings().get();
        int userTopicCount = 0;
        int internalTopicCount = 0;
        var topicNames = new ArrayList<String>();
        for (var listing : topicListings) {
            if (listing.isInternal()) {
                internalTopicCount++;
            } else {
                userTopicCount++;
            }
            topicNames.add(listing.name());
        }

        Map<String, TopicDescription> descriptions = topicNames.isEmpty() ? Map.of()
                                                                          : describeTopicsInBatches(client, topicNames);
        var topics = summarizeUserTopics(descriptions);
        int totalPartitions = descriptions.values().stream().mapToInt(desc -> desc.partitions().size()).sum();

        var partitionLastOffsets = fetchLastOffsets(client, descriptions);
        var partitions = PartitionHealthAnalyzer.summarizeAll(descriptions, partitionLastOffsets);
        int underReplicatedCount = (int) partitions.stream()
                                                   .filter(partition -> partition.issues()
                                                                                 .contains(PartitionHealthIssue.IssueType.UNDER_REPLICATED))
                                                   .count();
        int offlineCount = (int) partitions.stream()
                                           .filter(partition -> partition.issues()
                                                                         .contains(PartitionHealthIssue.IssueType.OFFLINE))
                                           .count();

        Map<String, Integer> consumerGroupsByState = new HashMap<>();
        client.listConsumerGroups().all().get().forEach(listing -> {
            var groupState = listing.groupState().map(Object::toString);
            var consumerState = listing.state().map(Object::toString);
            consumerGroupsByState.merge(groupState.or(() -> consumerState).orElse("-"), 1, Integer::sum);
        });

        var logDirs = describeLogDirs(client, nodes);
        var replicationStats = computeReplicationStats(descriptions, nodes.size());
        var schemaRegistryStatus = SchemaRegistryHealthService.statusForUrl(schemaRegistryUrl);

        var summary = new ClusterSummary(clusterId, controllerId, nodes.size(), userTopicCount, internalTopicCount,
                                         totalPartitions, underReplicatedCount, offlineCount, consumerGroupsByState,
                                         schemaRegistryStatus);
        return new ClusterMonitorSnapshot(summary, brokers, topics, partitions, logDirs, replicationStats);
    }

    private List<ClusterTopicSummary> summarizeUserTopics(Map<String, TopicDescription> descriptions) {
        return descriptions.values()
                           .stream()
                           .filter(description -> !description.isInternal())
                           .map(description -> {
                               var partitions = description.partitions();
                               int replicationFactor = partitions.isEmpty() ? 0 : partitions.getFirst().replicas().size();
                               return new ClusterTopicSummary(description.name(), partitions.size(), replicationFactor);
                           })
                           .sorted(Comparator.comparing(ClusterTopicSummary::name))
                           .collect(Collectors.toList());
    }

}
