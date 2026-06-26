package io.vepo.kafka.tool.inspect;

import static java.util.Collections.emptyList;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.inspect.bridge.KafkaAdminBridge;
import io.vepo.kafka.tool.inspect.bridge.impl.HttpSchemaRegistryBridge;
import io.vepo.kafka.tool.inspect.bridge.impl.KafkaClientsAdminBridge;
import io.vepo.kafka.tool.settings.KafkaBroker;

public class KafkaAdminService implements Closeable {

    @FunctionalInterface
    private interface BridgeTask {
        void run() throws Exception;
    }

    public enum BrokerStatus {
        IDLE, CONNECTED
    }

    public interface KafkaConnectionWatcher {

        void statusChanged(BrokerStatus status);
    }

    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminService.class);
    private final KafkaAdminBridge adminBridge;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<KafkaConnectionWatcher> watchers = new java.util.ArrayList<>();
    private BrokerStatus status = BrokerStatus.IDLE;

    private KafkaBroker connectedBroker;

    public KafkaAdminService() {
        this(KafkaClientsAdminBridge.create(HttpSchemaRegistryBridge.create()));
    }

    KafkaAdminService(KafkaAdminBridge adminBridge) {
        this.adminBridge = adminBridge;
    }

    @Override
    public void close() {
        logger.info("Closing client...");
        adminBridge.close();
        connectedBroker = null;
        status = BrokerStatus.IDLE;
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

    public void computeConsumerGroupLag(String groupId, Consumer<List<PartitionLagRow>> callback) {
        runOnBridge(() -> callback.accept(adminBridge.computeConsumerGroupLag(groupId)),
                    error -> {
                        logger.error("Could not compute consumer group lag!", error);
                        callback.accept(emptyList());
                    });
    }

    public void connect(KafkaBroker kafkaBroker, Consumer<ConnectionResult> callback) {
        executor.submit(() -> {
            try {
                adminBridge.connect(kafkaBroker);
                connectedBroker = kafkaBroker;
                status = BrokerStatus.CONNECTED;
                callback.accept(ConnectionResult.connected(kafkaBroker.getName()));
                watchers.forEach(watcher -> watcher.statusChanged(status));
            } catch (Exception e) {
                logger.error("Could not connect to broker!", e);
                adminBridge.disconnect();
                connectedBroker = null;
                status = BrokerStatus.IDLE;
                callback.accept(ConnectionResult.failed(e));
            }
        });
    }

    public KafkaBroker connectedBroker() {
        if (connectedBroker == null) {
            throw new IllegalStateException("Not connected to a broker.");
        }
        return connectedBroker.clone();
    }

    public void describeBrokerConfig(int brokerId, Consumer<List<BrokerConfigEntry>> callback) {
        runOnBridge(() -> callback.accept(adminBridge.describeBrokerConfig(brokerId)),
                    error -> {
                        logger.error("Could not describe broker config!", error);
                        callback.accept(emptyList());
                    });
    }

    public void describeConsumerGroupMembers(String groupId, Consumer<List<ConsumerGroupMemberInfo>> callback) {
        runOnBridge(() -> callback.accept(adminBridge.describeConsumerGroupMembers(groupId)),
                    error -> {
                        logger.error("Could not describe consumer group members!", error);
                        callback.accept(emptyList());
                    });
    }

    public void describeTopicPartitions(String topic, Consumer<List<TopicPartitionInfo>> callback) {
        runOnBridge(() -> callback.accept(adminBridge.describeTopicPartitions(topic)),
                    error -> {
                        logger.error("Could not describe topic partitions!", error);
                        callback.accept(emptyList());
                    });
    }

    public void disconnect(Consumer<BrokerStatus> callback) {
        executor.submit(() -> {
            adminBridge.disconnect();
            connectedBroker = null;
            status = BrokerStatus.IDLE;
            if (callback != null) {
                callback.accept(status);
            }
            watchers.forEach(watcher -> watcher.statusChanged(status));
        });
    }

    public void emptyTopic(TopicInfo topic) {
        executor.submit(() -> {
            logger.info("Cleaning topic... topic={}", topic);
            if (adminBridge.isConnected()) {
                try {
                    adminBridge.emptyTopic(topic);
                } catch (Exception e) {
                    logger.error("Error emptying topic!", e);
                }
            }
        });
    }

    public BrokerStatus getStatus() {
        return status;
    }

    public void listConsumerGroups(Consumer<List<ConsumerGroupSummary>> callback) {
        runOnBridge(() -> callback.accept(adminBridge.listConsumerGroups()),
                    error -> {
                        logger.error("Could not list consumer groups!", error);
                        callback.accept(emptyList());
                    });
    }

    public void listTopics(Consumer<List<TopicInfo>> callback) {
        executor.submit(() -> {
            if (adminBridge.isConnected()) {
                try {
                    callback.accept(adminBridge.listTopics());
                } catch (Exception e) {
                    logger.error("Could not list topics!", e);
                    callback.accept(emptyList());
                }
            } else {
                callback.accept(emptyList());
            }
        });
    }

    public void loadClusterMonitor(String schemaRegistryUrl, Consumer<ClusterMonitorSnapshot> callback) {
        runOnBridge(() -> callback.accept(adminBridge.loadClusterMonitor(schemaRegistryUrl)),
                    error -> {
                        logger.error("Could not load cluster monitor snapshot!", error);
                        callback.accept(null);
                    });
    }

    private void runOnBridge(BridgeTask task, Consumer<Throwable> errorHandler) {
        executor.submit(() -> {
            if (!adminBridge.isConnected()) {
                errorHandler.accept(new IllegalStateException("Not connected to a broker."));
                return;
            }
            try {
                task.run();
            } catch (Exception e) {
                errorHandler.accept(e);
            }
        });
    }

    public void testConnection(KafkaBroker kafkaBroker, Consumer<ConnectionResult> callback) {
        executor.submit(() -> {
            try {
                adminBridge.verifyClusterReachable(kafkaBroker);
                callback.accept(ConnectionResult.testOk(kafkaBroker.getName()));
            } catch (Exception e) {
                logger.error("Broker connection test failed!", e);
                callback.accept(ConnectionResult.failed(e));
            }
        });
    }

    public void watch(KafkaConnectionWatcher watcher) {
        watchers.add(watcher);
    }

}
