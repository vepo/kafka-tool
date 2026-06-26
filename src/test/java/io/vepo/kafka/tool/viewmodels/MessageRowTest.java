package io.vepo.kafka.tool.viewmodels;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.settings.KeySerializer;

class MessageRowTest {

    @Test
    void mapsPartitionOffsetAndTimestamp() throws Throwable {
        try (var env = feature("Message rows").scenario("Map consumer metadata to table row").start()) {
            var message = env.given("a Kafka message", new KafkaMessage("key".getBytes(), "{\"a\":1}"));
            var metadata = env.given("metadata with partition and timestamp",
                                     new MessageMetadata(2, 42L, 1700000000000L));
            var row = env.when("a message row is built",
                               () -> MessageRow.from(message, metadata, KeySerializer.STRING));
            env.then("partition offset and timestamp are preserved", () -> {
                assertEquals(2, row.getPartition());
                assertEquals(42L, row.getOffset());
                assertEquals(1700000000000L, row.getTimestamp());
            });
        }
    }

}
