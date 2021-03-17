package io.vepo.kt;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.clients.admin.RecordsToDelete.beforeOffset;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaAdminService implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminService.class);

    public enum BrokerStatus {
        IDLE, CONNECTED
    }

    public interface KafkaConnectionWatcher {

        public void statusChanged(BrokerStatus status);
    }

    private BrokerStatus status;
    private AdminClient adminClient = null;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<KafkaConnectionWatcher> watchers;

    public KafkaAdminService() {
        this.watchers = new ArrayList<>();
        this.status = BrokerStatus.IDLE;
    }

    public BrokerStatus getStatus() {
        return status;
    }

    public void connect(String boostraServer, Consumer<BrokerStatus> callback) {
        executor.submit(() -> {
            Properties properties = new Properties();
            properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, boostraServer);
            adminClient = AdminClient.create(properties);
            status = BrokerStatus.CONNECTED;
            callback.accept(status);
            watchers.forEach(consumer -> consumer.statusChanged(status));
        });
    }

    public void watch(KafkaConnectionWatcher watcher) {
        this.watchers.add(watcher);
    }

    public void emptyTopic(TopicInfo topic) {
        executor.submit(() -> {
            logger.info("Cleaning topic... topic={}", topic);
            if (nonNull(adminClient)) {
                logger.info("Describing topic... topic={}", topic);
                handle(adminClient.describeTopics(asList(topic.getName())).all(),
                       this::listOffsets,
                       error -> logger.error("Error describing topic!", error));
            }
        });
    }

    private static <T> void handle(KafkaFuture<T> operation, Consumer<T> successHandler,
            Consumer<Throwable> errorHandler) {
        operation.whenComplete((result, error) -> {
            if (isNull(error)) {
                errorHandler.accept(error);
            } else {
                successHandler.accept(result);
            }
        });
    }

    private void listOffsets(Map<String, TopicDescription> descs) {
        handle(adminClient.listOffsets(descs.values()
                                            .stream()
                                            .flatMap(desc -> desc.partitions()
                                                                 .stream()
                                                                 .map(partition -> new TopicPartition(desc.name(),
                                                                                                      partition.partition())))
                                            .collect(Collectors.toMap((TopicPartition t) -> t,
                                                                      t -> OffsetSpec.latest())))
                          .all(),
               this::deleteRecords,
               error -> logger.error("Could not list offset!", error));
    }

    private void deleteRecords(Map<TopicPartition, ListOffsetsResultInfo> listOffsetResults) {
        handle(adminClient.deleteRecords(listOffsetResults.entrySet()
                                                          .stream()
                                                          .collect(toMap(entry -> entry.getKey(),
                                                                         entry -> beforeOffset(entry.getValue()
                                                                                                    .offset()))))
                          .all(),
               KafkaAdminService::ignore,
               error -> logger.error("Error deleting records!", error));
    }

    private static <T> void ignore(T value) {

    }

    public void listTopics(Consumer<List<TopicInfo>> callback) {
        executor.submit(() -> {
            if (nonNull(adminClient)) {
                adminClient.listTopics()
                           .listings()
                           .whenComplete((topics, error) -> {
                               if (isNull(error)) {
                                   callback.accept(topics.stream()
                                                         .map(topic -> new TopicInfo(topic.name(), topic.isInternal()))
                                                         .collect(toList()));
                               } else {
                                   callback.accept(emptyList());
                               }
                           });
            } else {
                callback.accept(emptyList());
            }
        });
    }

    @Override
    public void close() {
        if (nonNull(adminClient)) {
            adminClient.close();
        }
    }

}
