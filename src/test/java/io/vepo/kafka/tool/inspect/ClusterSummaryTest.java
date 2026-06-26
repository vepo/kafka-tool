package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ClusterSummaryTest {

    @Test
    void consumerGroupCountSumsStates() throws Throwable {
        try (var env = feature("Cluster summary").scenario("Sum consumer group counts").start()) {
            var summary = new ClusterSummary("c", 1, 1, 0, 0, 0, 0, 0, Map.of("Stable", 3, "Empty", 2), "Not configured");

            env.then("total group count is sum of states", () -> assertEquals(5, summary.consumerGroupCount()));
        }
    }

    @Test
    void healthyWhenNoPartitionIssues() throws Throwable {
        try (var env = feature("Cluster summary").scenario("Healthy cluster has no issues").start()) {
            var summary = env.given("a cluster with no partition issues",
                                    new ClusterSummary("cluster-1", 1, 3, 5, 2, 20, 0, 0, Map.of("Stable", 4),
                                                       "Reachable"));

            env.then("cluster is healthy", () -> assertTrue(summary.healthy()));
            env.then("health summary mentions brokers",
                     () -> assertTrue(summary.healthSummary().contains("3 broker")));
        }
    }

    @Test
    void unhealthyWhenUnderReplicatedPartitionsExist() throws Throwable {
        try (var env = feature("Cluster summary").scenario("Unhealthy when URP exists").start()) {
            var summary = new ClusterSummary("cluster-1", 1, 3, 5, 2, 20, 2, 0, Map.of(), "Not configured");

            env.then("cluster is not healthy", () -> assertFalse(summary.healthy()));
            env.then("health summary mentions under-replicated partitions",
                     () -> assertTrue(summary.healthSummary().contains("under-replicated")));
        }
    }

}
