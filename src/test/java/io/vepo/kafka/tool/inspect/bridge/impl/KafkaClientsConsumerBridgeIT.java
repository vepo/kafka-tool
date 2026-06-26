package io.vepo.kafka.tool.inspect.bridge.impl;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static io.vepo.kafka.tool.support.KafkaTestFixtures.createTopic;
import static io.vepo.kafka.tool.support.KafkaTestFixtures.produceStrings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.inspect.FetchedRecord;
import io.vepo.kafka.tool.settings.ValueSerializer;
import io.vepo.kafka.tool.support.KafkaTestEnvironment;

@Tag("integration")
class KafkaClientsConsumerBridgeIT {

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
    void fetchPlainTextRecords() throws Throwable {
        try (var env = feature("Kafka consumer bridge").scenario("Fetch plain-text records").withKafkaBroker().start()) {
            var bridge = KafkaClientsConsumerBridge.create();
            var topicName = "bridge-fetch-" + System.nanoTime();
            createTopic(env.broker(), topicName, 1);
            produceStrings(env.broker(), topicName, List.of("alpha", "beta"));
            var records = env.when("records are fetched from offset 0",
                                   () -> run(() -> bridge.fetchRecords(env.broker(), topicName, 0, 0, 10,
                                                                       ValueSerializer.PLAIN_TEXT)));
            env.then("records are returned", () -> assertFalse(records.isEmpty()));
            env.then("first value matches", () -> assertEquals("alpha", records.getFirst().message().getValue()));
        }
    }

    @Test
    void liveConsumerReceivesProducedMessage() throws Throwable {
        try (var env = feature("Kafka consumer bridge").scenario("Live consumer receives message").withKafkaBroker().start()) {
            var bridge = KafkaClientsConsumerBridge.create();
            var topicName = "bridge-live-" + System.nanoTime();
            createTopic(env.broker(), topicName, 1);
            var received = new AtomicReference<String>();
            var latch = new CountDownLatch(1);
            var consumerThread = new Thread(() -> {
                try {
                    bridge.startLiveConsumer(env.broker(), topicName, ValueSerializer.PLAIN_TEXT, (metadata, message) -> {
                        received.set(message.getValue());
                        latch.countDown();
                        bridge.stopLiveConsumer();
                    });
                } catch (RuntimeException ignored) {
                    // stopped
                }
            });
            consumerThread.start();
            Thread.sleep(1_000);
            produceStrings(env.broker(), topicName, List.of("live-message"));
            env.then("consumer receives the message",
                     () -> assertTrue(run(() -> latch.await(20, TimeUnit.SECONDS))));
            env.then("payload matches", () -> assertEquals("live-message", received.get()));
            bridge.stopLiveConsumer();
            consumerThread.join(5_000);
        }
    }

    @Test
    void maxRecordsZeroReturnsEmpty() throws Throwable {
        try (var env = feature("Kafka consumer bridge").scenario("Zero max records returns empty").withKafkaBroker().start()) {
            var bridge = KafkaClientsConsumerBridge.create();
            List<FetchedRecord> records = env.when("fetch is called with zero max",
                                                   () -> run(() -> bridge.fetchRecords(env.broker(), "any", 0, 0, 0,
                                                                                       ValueSerializer.PLAIN_TEXT)));
            env.then("no records are returned", () -> assertTrue(records.isEmpty()));
        }
    }

}
