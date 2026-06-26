package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.Test;

class ReplicationStatsTest {

    @Test
    void averageReplicationFactorIsMean() throws Throwable {
        try (var env = feature("Replication stats").scenario("Average RF is mean of topic RFs").start()) {
            var leader = new Node(1, "host1", 9092);
            var replica = new Node(2, "host2", 9092);
            var rf1 = new TopicDescription("a", false, List.of(new TopicPartitionInfo(0, leader, List.of(leader), List.of(leader))));
            var rf2 = new TopicDescription("b", false,
                                           List.of(new TopicPartitionInfo(0, leader, List.of(leader, replica), List.of(leader, replica))));
            var stats = ReplicationStats.fromTopicDescriptions(Map.of("a", rf1, "b", rf2), 2);
            env.then("average is 1.5", () -> assertEquals(1.5, stats.averageReplicationFactor()));
        }
    }

    @Test
    void countsTopicsBelowBrokerCount() throws Throwable {
        try (var env = feature("Replication stats").scenario("Count topics below broker count").start()) {
            var leader = new Node(1, "host1", 9092);
            var replica = new Node(2, "host2", 9092);
            var rf1 = new TopicDescription("a", false, List.of(new TopicPartitionInfo(0, leader, List.of(leader), List.of(leader))));
            var rf2 = new TopicDescription("b", false,
                                           List.of(new TopicPartitionInfo(0, leader, List.of(leader, replica), List.of(leader, replica))));
            var stats = env.when("stats are computed for two topics",
                                 () -> ReplicationStats.fromTopicDescriptions(Map.of("a", rf1, "b", rf2), 3));
            env.then("both topics are below broker count", () -> assertEquals(2, stats.topicsBelowBrokerCount()));
            env.then("min and max RF are computed", () -> {
                assertEquals(1, stats.minReplicationFactor());
                assertEquals(2, stats.maxReplicationFactor());
            });
        }
    }

    @Test
    void emptyDescriptionsReturnZeros() throws Throwable {
        try (var env = feature("Replication stats").scenario("Empty topic map returns zeros").start()) {
            var stats = env.when("stats are computed", () -> ReplicationStats.fromTopicDescriptions(Map.of(), 3));
            env.then("all values are zero", () -> {
                assertEquals(0, stats.minReplicationFactor());
                assertEquals(0, stats.maxReplicationFactor());
                assertEquals(0, stats.averageReplicationFactor());
                assertEquals(0, stats.topicsBelowBrokerCount());
            });
        }
    }

}
