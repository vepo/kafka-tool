package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;

import io.vepo.kafka.tool.controllers.ConsumerGroupsController;
import io.vepo.kafka.tool.controls.EmptyStatePane;
import io.vepo.kafka.tool.controls.ViewActionBar;
import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.controls.builders.ScreenBuilder;
import io.vepo.kafka.tool.inspect.ConsumerGroupMemberInfo;
import io.vepo.kafka.tool.inspect.ConsumerGroupSummary;
import io.vepo.kafka.tool.inspect.PartitionLagRow;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ConsumerGroupsPane extends VBox {

    public ConsumerGroupsPane(ConsumerGroupsController controller) {
        super();
        setFillWidth(true);

        var viewHeader = new ViewHeader(
                                        "Consumer groups",
                                        "Inspect group membership and partition lag on the connected cluster.");
        viewHeader.bindMessage(controller.viewMessage());
        viewHeader.getStyleClass().add("main-window-view-header");
        VBox.setVgrow(viewHeader, Priority.NEVER);

        var gridBuilder = ScreenBuilder.grid();
        var groupsTable = gridBuilder.<ConsumerGroupSummary>addTableView(1)
                                     .<String>withColumn("Group ID")
                                     .fromProperty(ConsumerGroupSummary::groupId)
                                     .notEditable()
                                     .resizePolicy(grow(1))
                                     .add()
                                     .<String>withColumn("State")
                                     .fromProperty(ConsumerGroupSummary::state)
                                     .notEditable()
                                     .resizePolicy(fixedSize(96))
                                     .add()
                                     .build();
        groupsTable.setItems(controller.getGroups());
        groupsTable.getSelectionModel().selectedItemProperty()
                   .addListener((obs, oldValue, newValue) -> controller.selectGroup(newValue));
        groupsTable.setMinHeight(120);
        groupsTable.setMaxWidth(Double.MAX_VALUE);

        var membersTable = ScreenBuilder.grid()
                                        .<ConsumerGroupMemberInfo>addTableView(1)
                                        .<String>withColumn("Consumer ID")
                                        .fromProperty("consumerId")
                                        .notEditable()
                                        .resizePolicy(grow(1))
                                        .add()
                                        .<String>withColumn("Client ID")
                                        .fromProperty("clientId")
                                        .notEditable()
                                        .resizePolicy(grow(1))
                                        .add()
                                        .<String>withColumn("Host")
                                        .fromProperty("host")
                                        .notEditable()
                                        .resizePolicy(fixedSize(128))
                                        .add()
                                        .<String>withColumn("Assignment")
                                        .fromProperty("assignment")
                                        .notEditable()
                                        .resizePolicy(grow(2))
                                        .add()
                                        .build();
        membersTable.setItems(controller.getMembers());
        VBox.setVgrow(membersTable, Priority.ALWAYS);
        membersTable.setMinHeight(80);
        membersTable.setMaxWidth(Double.MAX_VALUE);

        var lagTable = ScreenBuilder.grid()
                                    .<PartitionLagRow>addTableView(1)
                                    .<String>withColumn("Topic")
                                    .fromProperty("topic")
                                    .notEditable()
                                    .resizePolicy(grow(1))
                                    .add()
                                    .<Integer>withColumn("Partition")
                                    .fromProperty("partition")
                                    .notEditable()
                                    .resizePolicy(fixedSize(80))
                                    .add()
                                    .<Long>withColumn("Committed")
                                    .fromProperty("committedOffset")
                                    .notEditable()
                                    .resizePolicy(fixedSize(96))
                                    .add()
                                    .<Long>withColumn("End")
                                    .fromProperty("endOffset")
                                    .notEditable()
                                    .resizePolicy(fixedSize(96))
                                    .add()
                                    .<Long>withColumn("Lag")
                                    .fromProperty("lag")
                                    .notEditable()
                                    .resizePolicy(fixedSize(80))
                                    .add()
                                    .build();
        lagTable.setItems(controller.getLagRows());
        VBox.setVgrow(lagTable, Priority.ALWAYS);
        lagTable.setMinHeight(80);
        lagTable.setMaxWidth(Double.MAX_VALUE);

        var emptyState = new EmptyStatePane("No consumer groups found.");
        var groupsStack = new StackPane(groupsTable, emptyState);
        groupsStack.setMaxWidth(Double.MAX_VALUE);
        emptyState.visibleProperty().bind(Bindings.isEmpty(controller.getGroups()));
        emptyState.managedProperty().bind(emptyState.visibleProperty());
        VBox.setVgrow(groupsStack, Priority.ALWAYS);

        var detailsBox = new VBox(10, membersTable, lagTable);
        detailsBox.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(detailsBox, Priority.ALWAYS);

        var contentGrid = gridBuilder.getGridPane();
        contentGrid.setMaxWidth(Double.MAX_VALUE);
        contentGrid.getChildren().clear();
        contentGrid.setPadding(new Insets(0));
        contentGrid.getColumnConstraints().clear();
        var leftColumn = new ColumnConstraints();
        leftColumn.setHgrow(Priority.ALWAYS);
        leftColumn.setFillWidth(true);
        var rightColumn = new ColumnConstraints();
        rightColumn.setHgrow(Priority.ALWAYS);
        rightColumn.setFillWidth(true);
        contentGrid.getColumnConstraints().addAll(leftColumn, rightColumn);

        contentGrid.add(groupsStack, 0, 0);
        contentGrid.add(detailsBox, 1, 0);
        GridPane.setHgrow(groupsStack, Priority.ALWAYS);
        GridPane.setHgrow(detailsBox, Priority.ALWAYS);
        GridPane.setVgrow(groupsStack, Priority.ALWAYS);
        GridPane.setVgrow(detailsBox, Priority.ALWAYS);
        GridPane.setFillWidth(groupsStack, true);
        GridPane.setFillWidth(detailsBox, true);
        VBox.setVgrow(contentGrid, Priority.ALWAYS);

        var refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> controller.refreshGroups());

        var autoRefresh = new CheckBox("Auto-refresh (5s)");
        autoRefresh.selectedProperty().bindBidirectional(controller.autoRefreshProperty());
        autoRefresh.setOnAction(e -> controller.setAutoRefresh(autoRefresh.isSelected()));

        var disconnectButton = new Button("Disconnect");
        disconnectButton.setOnAction(e -> controller.disconnect());

        var actionBar = new ViewActionBar(refreshButton, autoRefresh, disconnectButton);
        actionBar.setMaxHeight(actionBar.prefHeight(-1));
        VBox.setVgrow(actionBar, Priority.NEVER);

        getChildren().addAll(viewHeader, contentGrid, actionBar);
        controller.refreshGroups();
    }

}
