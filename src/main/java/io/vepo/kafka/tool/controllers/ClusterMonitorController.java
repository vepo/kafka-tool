package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.inspect.BrokerLogDirSummary;
import io.vepo.kafka.tool.inspect.ClusterBrokerInfo;
import io.vepo.kafka.tool.inspect.ClusterSummary;
import io.vepo.kafka.tool.inspect.ClusterTopicSummary;
import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import io.vepo.kafka.tool.inspect.ClusterPartitionSummary;
import io.vepo.kafka.tool.inspect.ReplicationStats;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class ClusterMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(ClusterMonitorController.class);

    private final KafkaAdminService adminService;
    private final Runnable disconnectAction;
    private final Consumer<String> navigateToTopic;
    private final BiConsumer<Integer, Stage> openBrokerConfig;
    private final ObservableList<ClusterBrokerInfo> brokers = observableArrayList();
    private final ObservableList<ClusterTopicSummary> topics = observableArrayList();
    private final ObservableList<ClusterPartitionSummary> partitions = observableArrayList();
    private final ObservableList<BrokerLogDirSummary> logDirs = observableArrayList();
    private final ObjectProperty<ClusterSummary> summary = new SimpleObjectProperty<>();
    private final ObjectProperty<ReplicationStats> replicationStats = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty autoRefresh = new SimpleBooleanProperty(false);
    private final ViewMessageModel viewMessage = new ViewMessageModel();
    private ScheduledExecutorService refreshScheduler;

    public ClusterMonitorController(KafkaAdminService adminService, Runnable disconnectAction,
                                    Consumer<String> navigateToTopic, BiConsumer<Integer, Stage> openBrokerConfig) {
        this.adminService = adminService;
        this.disconnectAction = disconnectAction;
        this.navigateToTopic = navigateToTopic;
        this.openBrokerConfig = openBrokerConfig;
        adminService.watch(state -> {
            if (state == BrokerStatus.CONNECTED) {
                refresh();
            } else {
                clear();
            }
        });
    }

    public BooleanProperty autoRefreshProperty() {
        return autoRefresh;
    }

    public void clear() {
        runLater(() -> {
            summary.set(null);
            replicationStats.set(null);
            brokers.clear();
            topics.clear();
            partitions.clear();
            logDirs.clear();
        });
    }

    public void disconnect() {
        disconnectAction.run();
    }

    public ObservableList<ClusterBrokerInfo> getBrokers() {
        return brokers;
    }

    public ObservableList<BrokerLogDirSummary> getLogDirs() {
        return logDirs;
    }

    public ObservableList<ClusterPartitionSummary> getPartitions() {
        return partitions;
    }

    public ObservableList<ClusterTopicSummary> getTopics() {
        return topics;
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void navigateToTopic(String topic) {
        navigateToTopic.accept(topic);
    }

    public void openBrokerConfig(int brokerId, Stage owner) {
        openBrokerConfig.accept(brokerId, owner);
    }

    public void refresh() {
        runLater(() -> loading.set(true));
        var schemaRegistryUrl = adminService.getStatus() == BrokerStatus.CONNECTED
                                                                                   ? adminService.connectedBroker().getSchemaRegistryUrl()
                                                                                   : "";
        adminService.loadClusterMonitor(schemaRegistryUrl, snapshot -> runLater(() -> {
            loading.set(false);
            if (snapshot == null) {
                viewMessage.showError("Could not load cluster information. Check the connection and try again.");
                return;
            }
            summary.set(snapshot.summary());
            replicationStats.set(snapshot.replicationStats());
            brokers.setAll(snapshot.brokers());
            topics.setAll(snapshot.topics());
            partitions.setAll(snapshot.partitions());
            logDirs.setAll(snapshot.logDirs());
            var clusterSummary = snapshot.summary();
            if (clusterSummary.healthy()) {
                viewMessage.showSuccess(clusterSummary.healthSummary());
            } else {
                viewMessage.showWarning(clusterSummary.healthSummary());
            }
            logger.info("Cluster monitor refreshed: brokers={}, partitions={}", snapshot.brokers().size(),
                        snapshot.partitions().size());
        }));
    }

    public ObjectProperty<ReplicationStats> replicationStatsProperty() {
        return replicationStats;
    }

    public void setAutoRefresh(boolean enabled) {
        autoRefresh.set(enabled);
        if (enabled) {
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    }

    public void shutdown() {
        stopAutoRefresh();
    }

    private void startAutoRefresh() {
        if (refreshScheduler == null) {
            refreshScheduler = Executors.newSingleThreadScheduledExecutor();
        }
        refreshScheduler.scheduleAtFixedRate(this::refresh, 30, 30, TimeUnit.SECONDS);
    }

    private void stopAutoRefresh() {
        if (refreshScheduler != null) {
            refreshScheduler.shutdownNow();
            refreshScheduler = null;
        }
    }

    public ObjectProperty<ClusterSummary> summaryProperty() {
        return summary;
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
