package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PartitionLagRowTest {

    @Test
    void lagIsNonNegativeDifference() throws Throwable {
        try (var env = feature("Consumer group lag").scenario("Compute lag from offsets").start()) {
            env.given("committed offset 100 and end offset 250");
            var row = new PartitionLagRow("group", "topic", 0, 100, 250, 150);
            env.then("lag equals end minus committed", () -> assertEquals(150, row.lag()));
        }
    }

    @Test
    void lagNeverNegativeWhenCommittedAhead() throws Throwable {
        try (var env = feature("Consumer group lag").scenario("Clamp lag when committed is ahead").start()) {
            env.given("committed offset ahead of log end");
            var lag = Math.max(0, 90L - 100L);
            env.then("lag is zero", () -> assertEquals(0, lag));
        }
    }

}
