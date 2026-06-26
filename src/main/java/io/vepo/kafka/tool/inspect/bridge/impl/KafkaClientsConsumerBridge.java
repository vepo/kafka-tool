package io.vepo.kafka.tool.inspect.bridge.impl;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Arrays.asList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.vepo.kafka.tool.consumers.AgnosticConsumerException;
import io.vepo.kafka.tool.consumers.ProtobufHelper;
import io.vepo.kafka.tool.consumers.RecordValueFormatter;
import io.vepo.kafka.tool.inspect.FetchedRecord;
import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.inspect.bridge.KafkaConsumerBridge;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

public final class KafkaClientsConsumerBridge implements KafkaConsumerBridge {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static Properties consumerProperties(KafkaBroker broker, ValueSerializer valueSerializer) {
        var configProperties = new Properties();
        configProperties.put(BOOTSTRAP_SERVERS_CONFIG, broker.getBootStrapServers());
        configProperties.put(GROUP_ID_CONFIG, "kafka-tool-" + UUID.randomUUID());
        configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, broker.getSchemaRegistryUrl());
        configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass(valueSerializer));
        return configProperties;
    }

    public static KafkaClientsConsumerBridge create() {
        return new KafkaClientsConsumerBridge();
    }

    private static Class<?> deserializerClass(ValueSerializer valueSerializer) {
        if (ValueSerializer.AVRO.equals(valueSerializer)) {
            return KafkaAvroDeserializer.class;
        }
        if (ValueSerializer.JSON.equals(valueSerializer)) {
            return KafkaJsonDeserializer.class;
        }
        if (ValueSerializer.PROTOBUF.equals(valueSerializer)) {
            return KafkaProtobufDeserializer.class;
        }
        return ByteArrayDeserializer.class;
    }

    private static String formatLiveValue(Object value, Class<?> deserializerClass) {
        if (deserializerClass.equals(KafkaJsonDeserializer.class) && value instanceof LinkedHashMap<?, ?> map) {
            try {
                return JSON_MAPPER.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                throw new AgnosticConsumerException(e);
            }
        }
        if (deserializerClass.equals(KafkaProtobufDeserializer.class) && value instanceof com.google.protobuf.Message proto) {
            return ProtobufHelper.toJson(proto);
        }
        return RecordValueFormatter.formatValue(value);
    }

    private static FetchedRecord toFetchedRecord(int partition, long offset, long timestamp, byte[] key,
                                                 Object valueObject) {
        return new FetchedRecord(new MessageMetadata(partition, offset, timestamp),
                                 new KafkaMessage(key, RecordValueFormatter.formatValue(valueObject)));
    }

    private final AtomicBoolean liveConsumerRunning = new AtomicBoolean(false);

    private KafkaClientsConsumerBridge() {}

    @Override
    public List<FetchedRecord> fetchRecords(KafkaBroker broker, String topic, int partition, long startOffset,
                                            int maxRecords, ValueSerializer valueSerializer)
            throws Exception {
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

    @Override
    public boolean isLiveConsumerRunning() {
        return liveConsumerRunning.get();
    }

    @Override
    public void startLiveConsumer(KafkaBroker broker, String topic, ValueSerializer valueSerializer,
                                  BiConsumer<MessageMetadata, KafkaMessage> onRecord) {
        liveConsumerRunning.set(true);
        var configProperties = consumerProperties(broker, valueSerializer);
        var deserializerClass = deserializerClass(valueSerializer);
        try (var consumer = new KafkaConsumer<byte[], Object>(configProperties)) {
            consumer.subscribe(asList(topic));
            while (liveConsumerRunning.get()) {
                consumer.poll(Duration.ofSeconds(1))
                        .forEach(record -> onRecord.accept(
                                                           new MessageMetadata(record.partition(), record.offset(), record.timestamp()),
                                                           new KafkaMessage(record.key(),
                                                                            formatLiveValue(record.value(), deserializerClass))));
            }
        } catch (Exception e) {
            liveConsumerRunning.set(false);
            throw new AgnosticConsumerException(e);
        }
    }

    @Override
    public void stopLiveConsumer() {
        liveConsumerRunning.set(false);
    }

}
