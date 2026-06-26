package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.inspect.PartitionHealthIssue.IssueType.LEADER_NOT_PREFERRED;
import static io.vepo.kafka.tool.inspect.PartitionHealthIssue.IssueType.OFFLINE;
import static io.vepo.kafka.tool.inspect.PartitionHealthIssue.IssueType.UNDER_REPLICATED;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;

public final class PartitionHealthAnalyzer {

    public static List<PartitionHealthIssue> analyze(TopicDescription description) {
        return analyze(description, Map.of());
    }

    public static List<PartitionHealthIssue> analyze(TopicDescription description,
                                                     Map<TopicPartition, Long> lastOffsets) {
        List<PartitionHealthIssue> issues = new ArrayList<>();
        for (TopicPartitionInfo partition : description.partitions()) {
            issues.addAll(analyzePartition(description.name(), partition, lastOffsets));
        }
        return issues;
    }

    public static List<PartitionHealthIssue> analyzeAll(Map<String, TopicDescription> descriptions) {
        return analyzeAll(descriptions, Map.of());
    }

    public static List<PartitionHealthIssue> analyzeAll(Map<String, TopicDescription> descriptions,
                                                        Map<TopicPartition, Long> lastOffsets) {
        return descriptions.values().stream()
                           .flatMap(description -> analyze(description, lastOffsets).stream())
                           .sorted(Comparator.comparing(PartitionHealthIssue::topic)
                                             .thenComparingInt(PartitionHealthIssue::partition))
                           .collect(Collectors.toList());
    }

    private static List<PartitionHealthIssue> analyzePartition(String topic, TopicPartitionInfo partition,
                                                               Map<TopicPartition, Long> lastOffsets) {
        List<PartitionHealthIssue> issues = new ArrayList<>();
        var leader = partition.leader();
        var replicas = partition.replicas();
        var isr = partition.isr();
        var leaderLabel = formatNode(leader);
        var replicasLabel = formatNodes(replicas);
        var isrLabel = formatNodes(isr);
        var lastOffset = lastOffsets.get(new TopicPartition(topic, partition.partition()));

        if (leader == null || isr.isEmpty()) {
            issues.add(new PartitionHealthIssue(topic, partition.partition(), leaderLabel, replicasLabel, isrLabel,
                                                OFFLINE, lastOffset));
            return issues;
        }

        if (isr.size() < replicas.size()) {
            issues.add(new PartitionHealthIssue(topic, partition.partition(), leaderLabel, replicasLabel, isrLabel,
                                                UNDER_REPLICATED, lastOffset));
        }

        if (!replicas.isEmpty() && leader.id() != replicas.getFirst().id()) {
            issues.add(new PartitionHealthIssue(topic, partition.partition(), leaderLabel, replicasLabel, isrLabel,
                                                LEADER_NOT_PREFERRED, lastOffset));
        }

        return issues;
    }

    private static String formatNode(Node node) {
        return node == null ? "—" : String.valueOf(node.id());
    }

    private static String formatNodes(Collection<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return "—";
        }
        return nodes.stream().map(Node::id).map(String::valueOf).collect(joining(", "));
    }

    public static List<ClusterPartitionSummary> summarizeAll(Map<String, TopicDescription> descriptions,
                                                             Map<TopicPartition, Long> lastOffsets) {
        List<ClusterPartitionSummary> rows = new ArrayList<>();
        for (var description : descriptions.values()) {
            for (var partitionInfo : description.partitions()) {
                String topic = description.name();
                int partitionId = partitionInfo.partition();
                var issues = analyzePartition(topic, partitionInfo, lastOffsets).stream()
                                                                                .map(PartitionHealthIssue::issueType)
                                                                                .toList();
                rows.add(new ClusterPartitionSummary(topic,
                                                     partitionId,
                                                     lastOffsets.get(new TopicPartition(topic, partitionId)),
                                                     formatNode(partitionInfo.leader()),
                                                     formatNodes(partitionInfo.replicas()),
                                                     formatNodes(partitionInfo.isr()),
                                                     issues));
            }
        }
        return rows.stream()
                   .sorted(Comparator.comparing(ClusterPartitionSummary::topic)
                                     .thenComparingInt(ClusterPartitionSummary::partition))
                   .collect(Collectors.toList());
    }

    private PartitionHealthAnalyzer() {}

}
