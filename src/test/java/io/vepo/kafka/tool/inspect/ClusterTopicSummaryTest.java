package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClusterTopicSummaryTest {

    @Test
    void exposesTopicMetadata() throws Throwable {
        try (var env = feature("Cluster topic summary").scenario("Expose topic metadata").start()) {
            var summary = new ClusterTopicSummary("orders", 12, 3);
            env.then("name partitions and RF are preserved", () -> {
                assertEquals("orders", summary.name());
                assertEquals(12, summary.partitions());
                assertEquals(3, summary.replicationFactor());
            });
        }
    }

}
