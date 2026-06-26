package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.inspect.ConsumerGroupFormatting.computeLag;
import static io.vepo.kafka.tool.inspect.ConsumerGroupFormatting.formatAssignment;
import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.apache.kafka.clients.admin.MemberAssignment;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

class ConsumerGroupFormattingTest {

    @Test
    void computeLagClampsNegativeValues() throws Throwable {
        try (var env = feature("Consumer group formatting").scenario("Clamp negative lag").start()) {
            env.then("lag is zero when committed ahead", () -> assertEquals(0, computeLag(100, 90)));
            env.then("lag is difference when behind", () -> assertEquals(50, computeLag(100, 150)));
        }
    }

    @Test
    void formatsEmptyAssignmentAsDash() throws Throwable {
        try (var env = feature("Consumer group formatting").scenario("Empty assignment shows dash").start()) {
            env.then("null assignment is dash", () -> assertEquals("-", formatAssignment(null)));
            env.then("empty assignment is dash",
                     () -> assertEquals("-", formatAssignment(new MemberAssignment(Set.of()))));
        }
    }

    @Test
    void formatsTopicPartitions() throws Throwable {
        try (var env = feature("Consumer group formatting").scenario("Format topic partitions").start()) {
            var assignment = new MemberAssignment(Set.of(new TopicPartition("orders", 0), new TopicPartition("orders", 1)));
            env.then("partitions are joined", () -> {
                var formatted = formatAssignment(assignment);
                assertTrue(formatted.contains("orders-0"));
                assertTrue(formatted.contains("orders-1"));
            });
        }
    }

}
