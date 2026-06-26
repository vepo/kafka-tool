package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;
import static io.vepo.kafka.tool.controls.builders.UI.actionBar;
import static io.vepo.kafka.tool.controls.builders.UI.mainView;
import static io.vepo.kafka.tool.controls.builders.UI.tableWithEmptyState;
import static io.vepo.kafka.tool.controls.builders.UI.twoColumns;
import static io.vepo.kafka.tool.controls.builders.UI.verticalSection;

import io.vepo.kafka.tool.controllers.ConsumerGroupsController;
import io.vepo.kafka.tool.controls.builders.UI;
import io.vepo.kafka.tool.inspect.ConsumerGroupMemberInfo;
import io.vepo.kafka.tool.inspect.ConsumerGroupSummary;
import io.vepo.kafka.tool.inspect.PartitionLagRow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ConsumerGroupsPane extends VBox {

    public ConsumerGroupsPane(ConsumerGroupsController controller) {
        super();
        setFillWidth(true);

        var groupsTable = UI.<ConsumerGroupSummary>table().withStringColumn("Group ID", ConsumerGroupSummary::groupId, grow(1))
                            .withStringColumn("State", ConsumerGroupSummary::state, fixedSize(96))
                            .items(controller.getGroups())
                            .onSelected(controller::selectGroup)
                            .minHeight(120)
                            .build();

        var membersTable = UI.<ConsumerGroupMemberInfo>table().withColumn("Consumer ID", ConsumerGroupMemberInfo::consumerId, grow(1))
                             .withColumn("Client ID", ConsumerGroupMemberInfo::clientId, grow(1))
                             .withColumn("Host", ConsumerGroupMemberInfo::host, fixedSize(128))
                             .withColumn("Assignment", ConsumerGroupMemberInfo::assignment, grow(2))
                             .items(controller.getMembers())
                             .minHeight(80)
                             .maxWidth(Double.MAX_VALUE)
                             .build();
        VBox.setVgrow(membersTable, Priority.ALWAYS);

        var lagTable = UI.<PartitionLagRow>table().withColumn("Topic", PartitionLagRow::topic, grow(1))
                         .withColumn("Partition", PartitionLagRow::partition, fixedSize(80))
                         .withColumn("Committed", PartitionLagRow::committedOffset, fixedSize(96))
                         .withColumn("End", PartitionLagRow::endOffset, fixedSize(96))
                         .withColumn("Lag", PartitionLagRow::lag, fixedSize(80))
                         .items(controller.getLagRows())
                         .minHeight(80)
                         .maxWidth(Double.MAX_VALUE)
                         .build();
        VBox.setVgrow(lagTable, Priority.ALWAYS);

        var groupsStack = tableWithEmptyState(groupsTable, controller.getGroups(), "No consumer groups found.");
        VBox.setVgrow(groupsStack, Priority.ALWAYS);

        var detailsBox = verticalSection(10, membersTable, lagTable);
        VBox.setVgrow(detailsBox, Priority.ALWAYS);

        var contentGrid = twoColumns(groupsStack, detailsBox);
        VBox.setVgrow(contentGrid, Priority.ALWAYS);

        var view = mainView().title("Consumer groups",
                                    "Inspect group membership and partition lag on the connected cluster.")
                             .mainWindowHeader()
                             .message(controller.viewMessage())
                             .body(contentGrid)
                             .actionBar(actionBar().refresh("Refresh", controller::refreshGroups)
                                                   .autoRefresh("Auto-refresh (5s)", controller::setAutoRefresh,
                                                                controller.autoRefreshProperty())
                                                   .disconnect("Disconnect", controller::disconnect)
                                                   .build())
                             .build();
        getChildren().setAll(view.getChildren());
        controller.refreshGroups();
    }

}
