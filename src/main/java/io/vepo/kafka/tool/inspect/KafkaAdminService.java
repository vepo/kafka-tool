package io.vepo.kafka.tool.inspect;

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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.settings.KafkaBroker;

public class KafkaAdminService implements Closeable {
    public interface AdminTask {
        void run(AdminClient client) throws Exception;
    }

    public enum BrokerStatus {
        IDLE, CONNECTED
    }

    public interface KafkaConnectionWatcher {

        void statusChanged(BrokerStatus status);
    }

    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminService.class);

    private static <T> void handle(KafkaFuture<T> operation, Consumer<T> successHandler,
                                   Consumer<Throwable> errorHandler) {
        operation.whenComplete((result, error) -> {
            if (nonNull(error)) {
                errorHandler.accept(error);
            } else {
                successHandler.accept(result);
            }
        });
    }

    private static <T> void ignore(T value) {}

    private final ConsumerGroupService consumerGroupService = ConsumerGroupService.create();
    private BrokerStatus status;
    private AdminClient adminClient = null;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<KafkaConnectionWatcher> watchers;

    private KafkaBroker connectedBroker;

    public KafkaAdminService() {
        this.watchers = new ArrayList<>();
        this.status = BrokerStatus.IDLE;
    }

    @Override
    public void close() {
        logger.info("Closing client...");
        closeAdminClient();
        connectedBroker = null;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2L, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private void closeAdminClient() {
        if (nonNull(adminClient)) {
            adminClient.close();
            adminClient = null;
        }
    }

    public void computeConsumerGroupLag(String groupId, Consumer<List<PartitionLagRow>> callback) {
        runOnAdminClient(client -> callback.accept(consumerGroupService.computeLag(client, groupId)),
                         error -> {
                             logger.error("Could not compute consumer group lag!", error);
                             callback.accept(emptyList());
                         });
    }

    public void connect(KafkaBroker kafkaBroker, Consumer<ConnectionResult> callback) {
        executor.submit(() -> {
            try {
                closeAdminClient();
                var properties = new Properties();
                properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootStrapServers());
                adminClient = AdminClient.create(properties);
                adminClient.describeCluster().nodes().get(10, TimeUnit.SECONDS);
                connectedBroker = kafkaBroker;
                status = BrokerStatus.CONNECTED;
                callback.accept(ConnectionResult.connected());
                watchers.forEach(consumer -> consumer.statusChanged(status));
            } catch (Exception e) {
                logger.error("Could not connect to broker!", e);
                closeAdminClient();
                connectedBroker = null;
                status = BrokerStatus.IDLE;
                callback.accept(ConnectionResult.failed(e.getMessage()));
            }
        });
    }

    public KafkaBroker connectedBroker() {
        if (connectedBroker == null) {
            throw new IllegalStateException("Not connected to a broker.");
        }
        return connectedBroker.clone();
    }

    private void deleteRecords(Map<TopicPartition, ListOffsetsResultInfo> listOffsetResults) {
        handle(adminClient.deleteRecords(listOffsetResults.entrySet()
                                                          .stream()
                                                          .collect(toMap(entry -> entry.getKey(),
                                                                         entry -> beforeOffset(entry.getValue().offset()))))
                          .all(),
               KafkaAdminService::ignore,
               error -> logger.error("Error deleting records!", error));
    }

    public void describeConsumerGroupMembers(String groupId, Consumer<List<ConsumerGroupMemberInfo>> callback) {
        runOnAdminClient(client -> callback.accept(consumerGroupService.describeMembers(client, groupId)),
                         error -> {
                             logger.error("Could not describe consumer group members!", error);
                             callback.accept(emptyList());
                         });
    }

    public void disconnect(Consumer<BrokerStatus> callback) {
        executor.submit(() -> {
            closeAdminClient();
            connectedBroker = null;
            status = BrokerStatus.IDLE;
            if (callback != null) {
                callback.accept(status);
            }
            watchers.forEach(consumer -> consumer.statusChanged(status));
        });
    }

    public void emptyTopic(TopicInfo topic) {
        executor.submit(() -> {
            logger.info("Cleaning topic... topic={}", topic);
            if (nonNull(adminClient)) {
                logger.info("Describing topic... topic={}", topic);
                handle(adminClient.describeTopics(asList(topic.getName())).allTopicNames(),
                       this::listOffsets,
                       error -> logger.error("Error describing topic!", error));
            }
        });
    }

    public BrokerStatus getStatus() {
        return status;
    }

    public void listConsumerGroups(Consumer<List<ConsumerGroupSummary>> callback) {
        runOnAdminClient(client -> callback.accept(consumerGroupService.listGroups(client)),
                         error -> {
                             logger.error("Could not list consumer groups!", error);
                             callback.accept(emptyList());
                         });
    }

    private void listOffsets(Map<String, TopicDescription> descs) {
        handle(adminClient.listOffsets(descs.values()
                                            .stream()
                                            .flatMap(desc -> desc.partitions()
                                                                 .stream()
                                                                 .map(partition -> new TopicPartition(desc.name(), partition.partition())))
                                            .collect(toMap((TopicPartition t) -> t, t -> OffsetSpec.latest())))
                          .all(),
               this::deleteRecords,
               error -> logger.error("Could not list offset!", error));
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

    public void runOnAdminClient(AdminTask task, Consumer<Throwable> errorHandler) {
        executor.submit(() -> {
            if (isNull(adminClient)) {
                errorHandler.accept(new IllegalStateException("Not connected to a broker."));
                return;
            }
            try {
                task.run(adminClient);
            } catch (Exception e) {
                errorHandler.accept(e);
            }
        });
    }

    public void testConnection(KafkaBroker kafkaBroker, Consumer<ConnectionResult> callback) {
        executor.submit(() -> {
            try {
                var properties = new Properties();
                properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootStrapServers());
                try (var client = AdminClient.create(properties)) {
                    client.describeCluster().nodes().get(10, TimeUnit.SECONDS);
                    callback.accept(ConnectionResult.testOk());
                }
            } catch (Exception e) {
                logger.error("Broker connection test failed!", e);
                callback.accept(ConnectionResult.failed(e.getMessage()));
            }
        });
    }

    public void watch(KafkaConnectionWatcher watcher) {
        this.watchers.add(watcher);
    }

}
