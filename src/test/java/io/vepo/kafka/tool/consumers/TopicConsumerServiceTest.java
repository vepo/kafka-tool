package io.vepo.kafka.tool.consumers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

class TopicConsumerServiceTest {

    @Test
    void availableValueSerializersWithoutSchemaRegistry() {
        var service = new TopicConsumerService();
        var broker = new KafkaBroker("local", "localhost:9092", "");
        var serializers = service.availableValueSerializers(broker);
        assertEquals(2, serializers.size());
        assertTrue(serializers.contains(ValueSerializer.JSON));
        assertTrue(serializers.contains(ValueSerializer.PLAIN_TEXT));
    }

    @Test
    void availableValueSerializersWithSchemaRegistry() {
        var service = new TopicConsumerService();
        var broker = new KafkaBroker("local", "localhost:9092", "http://localhost:8081");
        var serializers = service.availableValueSerializers(broker);
        assertEquals(4, serializers.size());
        assertTrue(serializers.contains(ValueSerializer.AVRO));
        assertTrue(serializers.contains(ValueSerializer.JSON));
        assertTrue(serializers.contains(ValueSerializer.PROTOBUF));
        assertTrue(serializers.contains(ValueSerializer.PLAIN_TEXT));
    }

    @Test
    void isNotRunningInitially() {
        var service = new TopicConsumerService();
        assertFalse(service.isRunning());
    }

}
