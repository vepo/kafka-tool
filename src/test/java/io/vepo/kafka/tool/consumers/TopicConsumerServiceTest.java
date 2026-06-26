package io.vepo.kafka.tool.consumers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

class TopicConsumerServiceTest {

    @Test
    void availableValueSerializersWithSchemaRegistry() {
	var service = new TopicConsumerService();
	var broker = new KafkaBroker("local", "localhost:9092", "http://localhost:8081");
	var serializers = service.availableValueSerializers(broker);
	assertEquals(3, serializers.size());
	assertTrue(serializers.contains(ValueSerializer.AVRO));
	assertTrue(serializers.contains(ValueSerializer.JSON));
	assertTrue(serializers.contains(ValueSerializer.PROTOBUF));
    }

    @Test
    void availableValueSerializersWithoutSchemaRegistry() {
	var service = new TopicConsumerService();
	var broker = new KafkaBroker("local", "localhost:9092", "");
	var serializers = service.availableValueSerializers(broker);
	assertEquals(1, serializers.size());
	assertEquals(ValueSerializer.JSON, serializers.get(0));
    }

    @Test
    void consumerForAvro() {
	var service = new TopicConsumerService();
	assertTrue(service.consumerFor(ValueSerializer.AVRO) instanceof KafkaAgnosticConsumer);
    }

    @Test
    void consumerForUnknownReturnsNull() {
	var service = new TopicConsumerService();
	assertNull(service.consumerFor(null));
    }

}
