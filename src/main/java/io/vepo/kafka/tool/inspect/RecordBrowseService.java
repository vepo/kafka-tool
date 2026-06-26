package io.vepo.kafka.tool.inspect;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;

import io.vepo.kafka.tool.consumers.RecordFetcher;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

public class RecordBrowseService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final KafkaAdminService adminService;

    public RecordBrowseService(KafkaAdminService adminService) {
        this.adminService = adminService;
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
        adminService.runOnAdminClient(client -> {
            var descriptions = client.describeTopics(List.of(topic)).allTopicNames().get();
            var description = descriptions.get(topic);
            if (description == null) {
                callback.accept(List.of());
                return;
            }
            Map<TopicPartition, OffsetSpec> earliest = description.partitions().stream()
                                                                  .map(info -> new TopicPartition(topic, info.partition()))
                                                                  .collect(toMap(tp -> tp, tp -> OffsetSpec.earliest()));
            Map<TopicPartition, OffsetSpec> latest = description.partitions().stream()
                                                                .map(info -> new TopicPartition(topic, info.partition()))
                                                                .collect(toMap(tp -> tp, tp -> OffsetSpec.latest()));
            var beginning = client.listOffsets(earliest).all().get();
            var end = client.listOffsets(latest).all().get();
            var partitions = description.partitions().stream()
                                        .map(info -> {
                                            var tp = new TopicPartition(topic, info.partition());
                                            long beginningOffset = beginning.get(tp).offset();
                                            long endOffset = end.get(tp).offset();
                                            return new TopicPartitionInfo(info.partition(), beginningOffset, endOffset);
                                        })
                                        .toList();
            callback.accept(partitions);
        }, error -> callback.accept(List.of()));
    }

    public void fetchRecords(KafkaBroker broker, String topic, int partition, long startOffset, int maxRecords,
                             ValueSerializer valueSerializer, Consumer<List<RecordFetcher.FetchedRecord>> callback,
                             Consumer<Throwable> onError) {
        executor.submit(() -> {
            try {
                callback.accept(
                                RecordFetcher.fetch(broker, topic, partition, startOffset, maxRecords, valueSerializer));
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

}
