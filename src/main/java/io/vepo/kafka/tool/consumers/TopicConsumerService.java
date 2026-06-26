package io.vepo.kafka.tool.consumers;

import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

public class TopicConsumerService {

    private final ExecutorService consumerExecutor = newSingleThreadExecutor();
    private KafkaAgnosticConsumer selectedConsumer;

    public List<ValueSerializer> availableValueSerializers(KafkaBroker broker) {
	if (broker.hasSchemaRegistry()) {
	    return asList(ValueSerializer.AVRO, ValueSerializer.JSON, ValueSerializer.PROTOBUF);
	}
	return asList(ValueSerializer.JSON);
    }

    public KafkaAgnosticConsumer consumerFor(ValueSerializer serializer) {
	if (ValueSerializer.AVRO.equals(serializer)) {
	    return KafkaAgnosticConsumer.avro();
	} else if (ValueSerializer.JSON.equals(serializer)) {
	    return KafkaAgnosticConsumer.json();
	} else if (ValueSerializer.PROTOBUF.equals(serializer)) {
	    return KafkaAgnosticConsumer.protobuf();
	}
	return null;
    }

    public boolean isRunning() {
	return selectedConsumer != null && selectedConsumer.isRunning();
    }

    public void start(KafkaBroker broker, String topic, ValueSerializer valueSerializer,
	    BiConsumer<MessageMetadata, KafkaMessage> onRecord, Runnable onStopped,
	    java.util.function.Consumer<AgnosticConsumerException> onError) {
	selectedConsumer = consumerFor(valueSerializer);
	consumerExecutor.submit(() -> {
	    try {
		selectedConsumer.start(broker, topic, onRecord);
		onStopped.run();
	    } catch (AgnosticConsumerException e) {
		onError.accept(e);
	    }
	});
    }

    public void stop() {
	if (selectedConsumer != null) {
	    selectedConsumer.stop();
	}
    }

    public void close() {
	stop();
	consumerExecutor.shutdown();
	try {
	    consumerExecutor.awaitTermination(2L, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	}
    }

}
