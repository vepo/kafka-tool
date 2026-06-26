package io.vepo.kafka.tool.inspect.bridge;

import java.util.List;
import java.util.function.BiConsumer;

import io.vepo.kafka.tool.inspect.FetchedRecord;
import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

public interface KafkaConsumerBridge {

    List<FetchedRecord> fetchRecords(KafkaBroker broker, String topic, int partition, long startOffset, int maxRecords,
                                     ValueSerializer valueSerializer)
            throws Exception;

    boolean isLiveConsumerRunning();

    void startLiveConsumer(KafkaBroker broker, String topic, ValueSerializer valueSerializer,
                           BiConsumer<MessageMetadata, KafkaMessage> onRecord);

    void stopLiveConsumer();

}
