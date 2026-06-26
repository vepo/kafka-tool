package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ClusterPartitionSummaryTest {

    @Test
    void healthyPartitionStatusDisplay() throws Throwable {
        try (var env = feature("Cluster partition summary").scenario("Healthy partition status").start()) {
            var summary = new ClusterPartitionSummary("orders", 0, 42L, "1", "1", "1", List.of());
            env.then("status is Healthy", () -> assertEquals("Healthy", summary.statusDisplay()));
            env.then("last offset is displayed", () -> assertEquals("42", summary.lastOffsetDisplay()));
        }
    }

    @Test
    void unhealthyPartitionStatusDisplay() throws Throwable {
        try (var env = feature("Cluster partition summary").scenario("Unhealthy partition status").start()) {
            var summary = new ClusterPartitionSummary("orders", 0, null, "1", "1, 2", "1",
                                                      List.of(PartitionHealthIssue.IssueType.UNDER_REPLICATED,
                                                              PartitionHealthIssue.IssueType.OFFLINE));
            env.then("status lists issues", () -> assertEquals("Under-replicated, Offline", summary.statusDisplay()));
            env.then("missing offset shows dash", () -> assertEquals("-", summary.lastOffsetDisplay()));
        }
    }

}
