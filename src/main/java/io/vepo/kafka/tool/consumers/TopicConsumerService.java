package io.vepo.kafka.tool.consumers;

import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.inspect.bridge.KafkaConsumerBridge;
import io.vepo.kafka.tool.inspect.bridge.impl.KafkaClientsConsumerBridge;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

public class TopicConsumerService {

    private final ExecutorService consumerExecutor = newSingleThreadExecutor();
    private final KafkaConsumerBridge consumerBridge;

    public TopicConsumerService() {
        this(KafkaClientsConsumerBridge.create());
    }

    TopicConsumerService(KafkaConsumerBridge consumerBridge) {
        this.consumerBridge = consumerBridge;
    }

    public List<ValueSerializer> availableValueSerializers(KafkaBroker broker) {
        if (broker.hasSchemaRegistry()) {
            return asList(ValueSerializer.AVRO, ValueSerializer.JSON, ValueSerializer.PROTOBUF, ValueSerializer.PLAIN_TEXT);
        }
        return asList(ValueSerializer.JSON, ValueSerializer.PLAIN_TEXT);
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

    public boolean isRunning() {
        return consumerBridge.isLiveConsumerRunning();
    }

    public void start(KafkaBroker broker, String topic, ValueSerializer valueSerializer,
                      BiConsumer<MessageMetadata, KafkaMessage> onRecord, Runnable onStopped,
                      java.util.function.Consumer<AgnosticConsumerException> onError) {
        consumerExecutor.submit(() -> {
            try {
                consumerBridge.startLiveConsumer(broker, topic, valueSerializer, onRecord);
                onStopped.run();
            } catch (AgnosticConsumerException e) {
                onError.accept(e);
            }
        });
    }

    public void stop() {
        consumerBridge.stopLiveConsumer();
    }

}
