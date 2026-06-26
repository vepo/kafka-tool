package io.vepo.kafka.tool.inspect.bridge.impl;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static io.vepo.kafka.tool.support.KafkaTestFixtures.createTopic;
import static io.vepo.kafka.tool.support.KafkaTestFixtures.produceStrings;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.inspect.TopicInfo;
import io.vepo.kafka.tool.support.KafkaTestEnvironment;

@Tag("integration")
class KafkaClientsAdminBridgeIT {

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    private static void run(CheckedRunnable action) {
        try {
            action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T run(CheckedSupplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void startContainers() {
        KafkaTestEnvironment.ensureStarted();
    }

    @Test
    void connectAndListTopics() throws Throwable {
        try (var env = feature("Kafka admin bridge").scenario("Connect and list topics").withKafkaBroker().withSchemaRegistry().start()) {
            var bridge = KafkaClientsAdminBridge.create(HttpSchemaRegistryBridge.create());
            env.when("the bridge connects", () -> run(() -> bridge.connect(env.broker())));
            var topics = env.when("topics are listed", () -> run(() -> bridge.listTopics()));
            env.then("connection is active", () -> assertTrue(bridge.isConnected()));
            env.then("topic list is returned", () -> assertNotNull(topics));
            bridge.disconnect();
        }
    }

    @Test
    void describeBrokerConfig() throws Throwable {
        try (var env = feature("Kafka admin bridge").scenario("Describe broker runtime config").withKafkaBroker().start()) {
            var bridge = KafkaClientsAdminBridge.create(HttpSchemaRegistryBridge.create());
            bridge.connect(env.broker());
            var config = env.when("broker config is loaded", () -> run(() -> bridge.describeBrokerConfig(1)));
            env.then("config entries exist", () -> assertFalse(config.isEmpty()));
            bridge.disconnect();
        }
    }

    @Test
    void describeTopicPartitionsAndEmptyTopic() throws Throwable {
        try (var env = feature("Kafka admin bridge").scenario("Describe partitions and empty topic").withKafkaBroker().start()) {
            var bridge = KafkaClientsAdminBridge.create(HttpSchemaRegistryBridge.create());
            bridge.connect(env.broker());
            var topicName = "bridge-browse-" + System.nanoTime();
            createTopic(env.broker(), topicName, 1);
            produceStrings(env.broker(), topicName, List.of("one", "two"));
            var partitions = env.when("partitions are described", () -> run(() -> bridge.describeTopicPartitions(topicName)));
            env.then("partition offsets are available", () -> assertFalse(partitions.isEmpty()));
            env.when("topic is emptied", () -> run(() -> bridge.emptyTopic(new TopicInfo(topicName, false))));
            Thread.sleep(2_000);
            var afterEmpty = run(() -> bridge.describeTopicPartitions(topicName));
            env.then("end offset is available after empty", () -> assertTrue(afterEmpty.getFirst().endOffset() >= 0));
            bridge.disconnect();
        }
    }

    @Test
    void loadClusterMonitorSnapshot() throws Throwable {
        try (var env = feature("Kafka admin bridge").scenario("Load cluster monitor snapshot").withKafkaBroker().withSchemaRegistry().start()) {
            var bridge = KafkaClientsAdminBridge.create(HttpSchemaRegistryBridge.create());
            bridge.connect(env.broker());
            createTopic(env.broker(), "bridge-monitor-topic", 1);
            var snapshot = env.when("cluster monitor loads",
                                    () -> run(() -> bridge.loadClusterMonitor(env.broker().getSchemaRegistryUrl())));
            env.then("snapshot includes brokers", () -> assertFalse(snapshot.brokers().isEmpty()));
            env.then("schema registry is reachable",
                     () -> assertTrue(snapshot.summary().schemaRegistryStatus().contains("Reachable")));
            bridge.disconnect();
        }
    }

    @Test
    void testConnectionWithoutSession() throws Throwable {
        try (var env = feature("Kafka admin bridge").scenario("Test connection without session").withKafkaBroker().start()) {
            var bridge = KafkaClientsAdminBridge.create(HttpSchemaRegistryBridge.create());
            env.when("cluster reachability is verified", () -> run(() -> bridge.verifyClusterReachable(env.broker())));
            env.then("bridge is not connected", () -> assertFalse(bridge.isConnected()));
        }
    }

}
