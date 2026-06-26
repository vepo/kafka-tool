package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;
import static io.vepo.kafka.tool.controls.builders.UI.actionBar;
import static io.vepo.kafka.tool.controls.builders.UI.bindInt;
import static io.vepo.kafka.tool.controls.builders.UI.bindString;
import static io.vepo.kafka.tool.controls.builders.UI.mainView;
import static io.vepo.kafka.tool.controls.builders.UI.scroll;
import static io.vepo.kafka.tool.controls.builders.UI.section;
import static io.vepo.kafka.tool.controls.builders.UI.statCard;
import static io.vepo.kafka.tool.controls.builders.UI.summaryBar;
import static io.vepo.kafka.tool.controls.builders.UI.tableWithEmptyState;
import static io.vepo.kafka.tool.controls.builders.UI.verticalSection;

import io.vepo.kafka.tool.controllers.ClusterMonitorController;
import io.vepo.kafka.tool.controls.builders.UI;
import io.vepo.kafka.tool.inspect.BrokerLogDirSummary;
import io.vepo.kafka.tool.inspect.ClusterBrokerInfo;
import io.vepo.kafka.tool.inspect.ClusterSummary;
import io.vepo.kafka.tool.inspect.ClusterTopicSummary;
import io.vepo.kafka.tool.inspect.ClusterPartitionSummary;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClusterMonitorPane extends VBox {

    private static Stage ownerStage(javafx.scene.Node node) {
        return node.getScene() != null ? (Stage) node.getScene().getWindow() : null;
    }

    public ClusterMonitorPane(ClusterMonitorController controller) {
        super();
        setFillWidth(true);

        var summaryBar = summaryBar(
                                    statCard("Cluster ID", bindString(controller.summaryProperty(), value -> ((ClusterSummary) value).clusterId())),
                                    statCard("Controller",
                                             bindInt(controller.summaryProperty(), value -> ((ClusterSummary) value).controllerId())),
                                    statCard("Brokers", bindInt(controller.summaryProperty(), value -> ((ClusterSummary) value).brokerCount())),
                                    statCard("Topics", Bindings.createStringBinding(() -> {
                                        var summary = controller.summaryProperty().get();
                                        return summary == null ? "—"
                                                               : "%d + %d internal".formatted(summary.userTopicCount(),
                                                                                              summary.internalTopicCount());
                                    }, controller.summaryProperty())),
                                    statCard("Partitions",
                                             bindInt(controller.summaryProperty(), value -> ((ClusterSummary) value).totalPartitions())),
                                    statCard("URP",
                                             bindInt(controller.summaryProperty(),
                                                     value -> ((ClusterSummary) value).underReplicatedCount())),
                                    statCard("Offline",
                                             bindInt(controller.summaryProperty(), value -> ((ClusterSummary) value).offlineCount())),
                                    statCard("Groups",
                                             bindInt(controller.summaryProperty(), value -> ((ClusterSummary) value).consumerGroupCount())),
                                    statCard("Schema Registry",
                                             bindString(controller.summaryProperty(),
                                                        value -> ((ClusterSummary) value).schemaRegistryStatus())),
                                    statCard("Replication", Bindings.createStringBinding(() -> {
                                        var stats = controller.replicationStatsProperty().get();
                                        if (stats == null) {
                                            return "—";
                                        }
                                        return "%d–%d (avg %.1f)".formatted(stats.minReplicationFactor(),
                                                                            stats.maxReplicationFactor(),
                                                                            stats.averageReplicationFactor());
                                    }, controller.replicationStatsProperty())));

        @SuppressWarnings("unchecked")
        TableView<ClusterBrokerInfo>[] brokersTableRef = new TableView[1];
        brokersTableRef[0] = UI.<ClusterBrokerInfo>table().withColumn("ID", ClusterBrokerInfo::brokerId, fixedSize(56))
                               .withColumn("Host", ClusterBrokerInfo::endpoint, grow(1))
                               .withColumn("Rack", ClusterBrokerInfo::rackDisplay, fixedSize(96))
                               .withColumn("Role", ClusterBrokerInfo::role, fixedSize(96))
                               .withActions("Actions")
                               .button("Config", broker -> controller.openBrokerConfig(broker.brokerId(),
                                                                                       ownerStage(brokersTableRef[0])))
                               .width(fixedSize(96))
                               .add()
                               .items(controller.getBrokers())
                               .minHeight(100)
                               .prefHeight(140)
                               .maxWidth(Double.MAX_VALUE)
                               .build();

        var brokersStack = tableWithEmptyState(brokersTableRef[0], controller.getBrokers(), "No brokers found.");

        var topicsTable = UI.<ClusterTopicSummary>table().withStringColumn("Topic", ClusterTopicSummary::name, grow(1))
                            .withColumn("Partitions", ClusterTopicSummary::partitions, fixedSize(88))
                            .withColumn("Replication factor", ClusterTopicSummary::replicationFactor, fixedSize(120))
                            .withActions("Actions")
                            .button("View topic", topic -> controller.navigateToTopic(topic.name()))
                            .width(fixedSize(96))
                            .add()
                            .items(controller.getTopics())
                            .minHeight(100)
                            .prefHeight(160)
                            .maxWidth(Double.MAX_VALUE)
                            .build();

        var topicsStack = tableWithEmptyState(topicsTable, controller.getTopics(), "No user topics found.");

        var logDirsTable = UI.<BrokerLogDirSummary>table().withColumn("Broker", BrokerLogDirSummary::brokerId, fixedSize(64))
                             .withColumn("Log dir", BrokerLogDirSummary::logDir, grow(1))
                             .withColumn("Size",
                                         row -> row.error() == null ? row.formattedSize() : row.error(),
                                         fixedSize(120))
                             .items(controller.getLogDirs())
                             .minHeight(80)
                             .prefHeight(120)
                             .maxWidth(Double.MAX_VALUE)
                             .build();

        var logDirsStack = tableWithEmptyState(logDirsTable, controller.getLogDirs(), "No log directory data.");

        var partitionsTable = UI.<ClusterPartitionSummary>table().withColumn("Topic", ClusterPartitionSummary::topic, grow(1))
                                .withColumn("Partition", ClusterPartitionSummary::partition, fixedSize(72))
                                .withColumn("Last offset", ClusterPartitionSummary::lastOffsetDisplay, fixedSize(96))
                                .withColumn("Status", ClusterPartitionSummary::statusDisplay, fixedSize(160))
                                .withColumn("Leader", ClusterPartitionSummary::leader, fixedSize(64))
                                .withColumn("Replicas", ClusterPartitionSummary::replicas, fixedSize(96))
                                .withColumn("ISR", ClusterPartitionSummary::isr, fixedSize(96))
                                .withActions("Actions")
                                .button("View topic", partition -> controller.navigateToTopic(partition.topic()))
                                .width(fixedSize(96))
                                .add()
                                .items(controller.getPartitions())
                                .minHeight(100)
                                .maxWidth(Double.MAX_VALUE)
                                .build();

        var partitionsStack = tableWithEmptyState(partitionsTable, controller.getPartitions(),
                                                  "No partitions found on the cluster.");
        VBox.setVgrow(partitionsStack, Priority.ALWAYS);

        var content = verticalSection(12, summaryBar, section("Brokers"), brokersStack, section("Topics"), topicsStack,
                                      section("Log directories"), logDirsStack, section("Partitions"), partitionsStack);
        VBox.setVgrow(content, Priority.ALWAYS);

        var view = mainView().title("Cluster",
                                    "Brokers, topics, partition layout, and storage on the connected cluster.")
                             .mainWindowHeader()
                             .message(controller.viewMessage())
                             .padding(new Insets(0, 12, 12, 12))
                             .spacing(10)
                             .body(scroll(content))
                             .actionBar(actionBar().refresh("Refresh", controller::refresh)
                                                   .autoRefresh("Auto-refresh (30s)", controller::setAutoRefresh,
                                                                controller.autoRefreshProperty())
                                                   .disconnect("Disconnect", controller::disconnect)
                                                   .build())
                             .build();
        getChildren().setAll(view.getChildren());
        controller.refresh();
    }

}
