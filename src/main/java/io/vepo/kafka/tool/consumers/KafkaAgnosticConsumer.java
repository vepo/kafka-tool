package io.vepo.kafka.tool.consumers;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Arrays.asList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.google.protobuf.Message;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.vepo.kafka.tool.controls.helpers.ProtobufHelper;
import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.settings.KafkaBroker;

public interface KafkaAgnosticConsumer {

    class AvroKafkaAgnosticConsumer implements KafkaAgnosticConsumer {
	private AtomicBoolean running;

	public AvroKafkaAgnosticConsumer() {
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
	    configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
	    configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
	    configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, broker.getSchemaRegistryUrl());
	    try (var consumer = new KafkaConsumer<String, GenericData.Record>(configProperties)) {
		consumer.subscribe(asList(topic));
		while (running.get()) {
		    consumer.poll(Duration.ofSeconds(1))
			    .forEach(record -> callback.accept(new MessageMetadata(record.offset()),
				    new KafkaMessage(record.key(), record.value().toString())));
		    try {
			Thread.sleep(500);
		    } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		    }
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

    class JsonKafkaAgnosticConsumer implements KafkaAgnosticConsumer {
	private AtomicBoolean running;

	public JsonKafkaAgnosticConsumer() {
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
	    configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonDeserializer.class);
	    configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
	    configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, broker.getSchemaRegistryUrl());
	    try (var consumer = new KafkaConsumer<String, String>(configProperties)) {
		consumer.subscribe(asList(topic));
		while (running.get()) {
		    consumer.poll(Duration.ofSeconds(1))
			    .forEach(record -> callback.accept(new MessageMetadata(record.offset()),
				    new KafkaMessage(record.key(), record.value().trim())));
		    try {
			Thread.sleep(500);
		    } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		    }
		}
	    } catch (Exception e) {
		throw new AgnosticConsumerException(e);
	    }
	}

	@Override
	public void stop() {
	    running.set(false);
	    running.set(false);
	}
    }

    class ProtobufKafkaAgnosticConsumer implements KafkaAgnosticConsumer {
	private AtomicBoolean running;

	public ProtobufKafkaAgnosticConsumer() {
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
	    configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
	    configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, broker.getSchemaRegistryUrl());
	    configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
	    try (var consumer = new KafkaConsumer<String, Message>(configProperties)) {
		consumer.subscribe(asList(topic));
		while (running.get()) {
		    consumer.poll(Duration.ofSeconds(1))
			    .forEach(record -> callback.accept(new MessageMetadata(record.offset()),
				    new KafkaMessage(record.key(), ProtobufHelper.toJson(record.value()))));
		    try {
			Thread.sleep(500);
		    } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		    }
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
