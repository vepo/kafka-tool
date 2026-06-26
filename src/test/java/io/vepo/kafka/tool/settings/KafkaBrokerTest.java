package io.vepo.kafka.tool.settings;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KafkaBrokerTest {

    @Test
    void cloneProducesEqualCopy() throws Throwable {
        try (var env = feature("Kafka broker profile").scenario("Clone broker profile").start()) {
            var original = new KafkaBroker("local", "localhost:9092", "http://localhost:8081");
            var copy = original.clone();
            env.then("clone is equal", () -> assertEquals(original, copy));
        }
    }

    @Test
    void hasNoSchemaRegistryWhenUrlBlank() throws Throwable {
        try (var env = feature("Kafka broker profile").scenario("Schema registry not configured").start()) {
            var broker = new KafkaBroker("local", "localhost:9092", "  ");
            env.then("has no schema registry", () -> assertFalse(broker.hasSchemaRegistry()));
        }
    }

    @Test
    void hasSchemaRegistryWhenUrlSet() throws Throwable {
        try (var env = feature("Kafka broker profile").scenario("Schema registry configured").start()) {
            var broker = new KafkaBroker("local", "localhost:9092", "http://localhost:8081");
            env.then("has schema registry", () -> assertTrue(broker.hasSchemaRegistry()));
        }
    }

}
