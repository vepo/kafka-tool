package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.Test;

class PartitionHealthAnalyzerTest {

    @Test
    void detectsLeaderNotOnPreferredReplica() throws Throwable {
        try (var env = feature("Cluster partition health").scenario("Detect leader not on preferred replica").start()) {
            var preferred = new Node(1, "host1", 9092);
            var leader = new Node(2, "host2", 9092);
            var partition = new TopicPartitionInfo(0, leader, List.of(preferred, leader), List.of(preferred, leader));
            var description = new TopicDescription("orders", false, List.of(partition));

            var issues = env.when("partition health is analyzed", () -> PartitionHealthAnalyzer.analyze(description));

            env.then("leader-not-preferred is reported",
                     () -> assertTrue(issues.stream()
                                            .anyMatch(i -> i.issueType() == PartitionHealthIssue.IssueType.LEADER_NOT_PREFERRED)));
        }
    }

    @Test
    void detectsOfflinePartition() throws Throwable {
        try (var env = feature("Cluster partition health").scenario("Detect offline partition").start()) {
            var replica = new Node(1, "host1", 9092);
            var partition = new TopicPartitionInfo(0, null, List.of(replica), List.of());
            var description = new TopicDescription("orders", false, List.of(partition));

            var issues = env.when("partition health is analyzed", () -> PartitionHealthAnalyzer.analyze(description));

            env.then("offline issue is reported", () -> assertTrue(issues.stream()
                                                                         .anyMatch(i -> i.issueType() == PartitionHealthIssue.IssueType.OFFLINE)));
        }
    }

    @Test
    void detectsUnderReplicatedPartition() throws Throwable {
        try (var env = feature("Cluster partition health").scenario("Detect under-replicated partition").start()) {
            var leader = new Node(1, "host1", 9092);
            var replica2 = new Node(2, "host2", 9092);
            var partition = new TopicPartitionInfo(0, leader, List.of(leader, replica2), List.of(leader));
            var description = new TopicDescription("orders", false, List.of(partition));

            var issues = env.when("partition health is analyzed", () -> PartitionHealthAnalyzer.analyze(description));

            env.then("under-replication is reported", () -> assertEquals(1, issues.stream()
                                                                                  .filter(i -> i.issueType() == PartitionHealthIssue.IssueType.UNDER_REPLICATED)
                                                                                  .count()));
        }
    }

    @Test
    void healthyPartitionHasNoIssues() throws Throwable {
        try (var env = feature("Cluster partition health").scenario("Healthy partition has no issues").start()) {
            var leader = new Node(1, "host1", 9092);
            var partition = new TopicPartitionInfo(0, leader, List.of(leader), List.of(leader));
            var description = new TopicDescription("orders", false, List.of(partition));

            var issues = env.when("partition health is analyzed", () -> PartitionHealthAnalyzer.analyze(description));

            env.then("no issues are reported", () -> assertTrue(issues.isEmpty()));
        }
    }

    @Test
    void includesLastOffsetWhenProvided() throws Throwable {
        try (var env = feature("Cluster partition health").scenario("Include last offset on partition issue").start()) {
            var leader = new Node(1, "host1", 9092);
            var replica2 = new Node(2, "host2", 9092);
            var partition = new TopicPartitionInfo(0, leader, List.of(leader, replica2), List.of(leader));
            var description = new TopicDescription("orders", false, List.of(partition));
            var offsets = Map.of(new TopicPartition("orders", 0), 42L);

            var issues = env.when("partition health is analyzed with offsets",
                                  () -> PartitionHealthAnalyzer.analyze(description, offsets));

            env.then("last offset is attached to the issue", () -> assertEquals(42L, issues.getFirst().lastOffset()));
        }
    }

    @Test
    void summarizeAllIncludesHealthyPartitions() throws Throwable {
        try (var env = feature("Cluster partition health").scenario("List all partitions including healthy").start()) {
            var leader = new Node(1, "host1", 9092);
            var partition = new TopicPartitionInfo(0, leader, List.of(leader), List.of(leader));
            var description = new TopicDescription("orders", false, List.of(partition));
            var offsets = Map.of(new TopicPartition("orders", 0), 42L);

            var partitions = env.when("partitions are summarized",
                                      () -> PartitionHealthAnalyzer.summarizeAll(Map.of("orders", description), offsets));

            env.then("healthy partition is listed with offset",
                     () -> {
                         assertEquals(1, partitions.size());
                         assertEquals("Healthy", partitions.getFirst().statusDisplay());
                         assertEquals(42L, partitions.getFirst().lastOffset());
                     });
        }
    }

}
