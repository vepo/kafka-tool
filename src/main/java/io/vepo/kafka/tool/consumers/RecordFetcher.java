package io.vepo.kafka.tool.consumers;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Arrays.asList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

public final class RecordFetcher {

    public record FetchedRecord(MessageMetadata metadata, KafkaMessage message) {}

    private static Properties consumerProperties(KafkaBroker broker, ValueSerializer valueSerializer) {
        var configProperties = new Properties();
        configProperties.put(BOOTSTRAP_SERVERS_CONFIG, broker.getBootStrapServers());
        configProperties.put(GROUP_ID_CONFIG, "browse-" + UUID.randomUUID());
        configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, broker.getSchemaRegistryUrl());
        configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass(valueSerializer));
        return configProperties;
    }

    private static Class<?> deserializerClass(ValueSerializer valueSerializer) {
        if (ValueSerializer.AVRO.equals(valueSerializer)) {
            return KafkaAvroDeserializer.class;
        } else if (ValueSerializer.JSON.equals(valueSerializer)) {
            return KafkaJsonDeserializer.class;
        } else if (ValueSerializer.PROTOBUF.equals(valueSerializer)) {
            return KafkaProtobufDeserializer.class;
        }
        return ByteArrayDeserializer.class;
    }

    public static List<FetchedRecord> fetch(KafkaBroker broker, String topic, int partition, long startOffset,
                                            int maxRecords, ValueSerializer valueSerializer) {
        if (maxRecords <= 0) {
            return List.of();
        }
        var configProperties = consumerProperties(broker, valueSerializer);
        try (var consumer = new KafkaConsumer<byte[], Object>(configProperties)) {
            var topicPartition = new TopicPartition(topic, partition);
            consumer.assign(asList(topicPartition));
            consumer.seek(topicPartition, startOffset);
            var results = new ArrayList<FetchedRecord>();
            while (results.size() < maxRecords) {
                var polled = consumer.poll(Duration.ofMillis(500));
                if (polled.isEmpty()) {
                    break;
                }
                for (var record : polled) {
                    results.add(toFetchedRecord(record.partition(), record.offset(), record.timestamp(),
                                                record.key(), record.value()));
                    if (results.size() >= maxRecords) {
                        break;
                    }
                }
            }
            return results;
        }
    }

    private static FetchedRecord toFetchedRecord(int partition, long offset, long timestamp, byte[] key,
                                                 Object valueObject) {
        String value;
        if (valueObject instanceof byte[] bytes) {
            value = new String(bytes, StandardCharsets.UTF_8);
        } else if (valueObject instanceof org.apache.avro.generic.GenericData.Record avro) {
            value = avro.toString();
        } else if (valueObject instanceof com.google.protobuf.Message proto) {
            value = ProtobufHelper.toJson(proto);
        } else if (valueObject instanceof java.util.LinkedHashMap<?, ?> map) {
            value = map.toString();
        } else {
            value = String.valueOf(valueObject);
        }
        return new FetchedRecord(new MessageMetadata(partition, offset, timestamp), new KafkaMessage(key, value));
    }

    private RecordFetcher() {}

}
