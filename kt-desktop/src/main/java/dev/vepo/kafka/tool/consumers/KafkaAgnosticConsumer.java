package dev.vepo.kafka.tool.consumers;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Arrays.asList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;

import dev.vepo.kafka.tool.controls.helpers.ProtobufHelper;
import dev.vepo.kafka.tool.core.model.KafkaBroker;
import dev.vepo.kafka.tool.inspect.KafkaMessage;
import dev.vepo.kafka.tool.inspect.MessageMetadata;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;

public interface KafkaAgnosticConsumer {


    abstract class AbstractKafkaAgnosticConsumer<R, T extends Deserializer> implements KafkaAgnosticConsumer {
        private final Class<T> deserializerClass;
        private final Function<R, String> mapper;
        private AtomicBoolean running;

        protected AbstractKafkaAgnosticConsumer(Class<T> deserializerClass, Function<R, String> mapper) {
            this.deserializerClass = deserializerClass;
            this.mapper = mapper;
            running = new AtomicBoolean(false);
        }

        @Override
        public boolean isRunning() {
            return running.get();
        }

        @Override
        public void start(KafkaBroker broker, String topic, BiConsumer<MessageMetadata, KafkaMessage> callback) {
            running.set(true);
            var configProperties = new Properties();
            configProperties.put(BOOTSTRAP_SERVERS_CONFIG, broker.getBootStrapServers());
            configProperties.put(GROUP_ID_CONFIG, "random-" + UUID.randomUUID().toString());
            configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
            configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass);
            configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
            configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, broker.getSchemaRegistryUrl());
            try (var consumer = new KafkaConsumer<byte[], R>(configProperties)) {
                consumer.subscribe(asList(topic));
                while (running.get()) {
                    consumer.poll(Duration.ofSeconds(1))
                            .forEach(record -> callback.accept(new MessageMetadata(record.offset()),
                                                               new KafkaMessage(record.key(), mapper.apply(record.value()))));
                }
            } catch (Exception e) {
                running.set(false);
                throw new AgnosticConsumerException(e);
            }
        }

        @Override
        public void stop() {
            running.set(false);
        }
    }

    class AvroKafkaAgnosticConsumer extends AbstractKafkaAgnosticConsumer<GenericData.Record, KafkaAvroDeserializer> {

        public AvroKafkaAgnosticConsumer() {
            super(KafkaAvroDeserializer.class, GenericData.Record::toString);
        }
    }

    class JsonKafkaAgnosticConsumer extends AbstractKafkaAgnosticConsumer<LinkedHashMap<String, Object>, KafkaJsonDeserializer> {

        private static final ObjectMapper mapper = new ObjectMapper();

        private static String jsonNode2String(LinkedHashMap<String, Object> node) {
            try {
                return mapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                throw new AgnosticConsumerException(e);
            }
        }

        public JsonKafkaAgnosticConsumer() {
            super(KafkaJsonDeserializer.class, JsonKafkaAgnosticConsumer::jsonNode2String);
        }
    }

    class ProtobufKafkaAgnosticConsumer extends AbstractKafkaAgnosticConsumer<Message, KafkaProtobufDeserializer> {

        public ProtobufKafkaAgnosticConsumer() {
            super(KafkaProtobufDeserializer.class, ProtobufHelper::toJson);
        }
    }

    static KafkaAgnosticConsumer avro() {
        return new AvroKafkaAgnosticConsumer();
    }

    static KafkaAgnosticConsumer json() {
        return new JsonKafkaAgnosticConsumer();
    }

    static KafkaAgnosticConsumer protobuf() {
        return new ProtobufKafkaAgnosticConsumer();
    }

    boolean isRunning();

    void start(KafkaBroker broker, String topic, BiConsumer<MessageMetadata, KafkaMessage> callback);

    void stop();

}
