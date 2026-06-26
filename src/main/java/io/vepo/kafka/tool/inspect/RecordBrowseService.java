package io.vepo.kafka.tool.inspect;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.vepo.kafka.tool.inspect.bridge.KafkaConsumerBridge;
import io.vepo.kafka.tool.inspect.bridge.impl.KafkaClientsConsumerBridge;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

public class RecordBrowseService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final KafkaAdminService adminService;
    private final KafkaConsumerBridge consumerBridge;

    public RecordBrowseService(KafkaAdminService adminService) {
        this(adminService, KafkaClientsConsumerBridge.create());
    }

    RecordBrowseService(KafkaAdminService adminService, KafkaConsumerBridge consumerBridge) {
        this.adminService = adminService;
        this.consumerBridge = consumerBridge;
    }

    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(2L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void describeTopicPartitions(String topic, Consumer<List<TopicPartitionInfo>> callback) {
        adminService.describeTopicPartitions(topic, callback);
    }

    public void fetchRecords(KafkaBroker broker, String topic, int partition, long startOffset, int maxRecords,
                             ValueSerializer valueSerializer, Consumer<List<FetchedRecord>> callback,
                             Consumer<Throwable> onError) {
        executor.submit(() -> {
            try {
                callback.accept(consumerBridge.fetchRecords(broker, topic, partition, startOffset, maxRecords,
                                                            valueSerializer));
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

}
