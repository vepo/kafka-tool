package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;
import static javafx.collections.FXCollections.observableArrayList;

import io.vepo.kafka.tool.controllers.ConsumerGroupsController;
import io.vepo.kafka.tool.controls.EmptyStatePane;
import io.vepo.kafka.tool.controls.ProgressStatusBar;
import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.controls.builders.ScreenBuilder;
import io.vepo.kafka.tool.inspect.ConsumerGroupMemberInfo;
import io.vepo.kafka.tool.inspect.ConsumerGroupSummary;
import io.vepo.kafka.tool.inspect.PartitionLagRow;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

public class ConsumerGroupsPane extends VBox {

    public ConsumerGroupsPane(ConsumerGroupsController controller) {
        super(10);
        setFillWidth(true);

        var viewHeader = new ViewHeader(
                                        "Consumer groups",
                                        "Inspect group membership and partition lag on the connected cluster.");
        viewHeader.bindMessage(controller.viewMessage());

        var progressBar = new ProgressStatusBar(10);
        progressBar.loadingProperty().bind(controller.loadingProperty());

        var refreshButton = new Button("Refresh");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(e -> controller.refreshGroups());

        var autoRefresh = new CheckBox("Auto-refresh (5s)");
        autoRefresh.selectedProperty().bindBidirectional(controller.autoRefreshProperty());
        autoRefresh.setOnAction(e -> controller.setAutoRefresh(autoRefresh.isSelected()));

        var groupsList = new ListView<ConsumerGroupSummary>();
        groupsList.setItems(controller.getGroups());
        groupsList.getSelectionModel().selectedItemProperty()
                  .addListener((obs, oldValue, newValue) -> controller.selectGroup(newValue));
        VBox.setVgrow(groupsList, Priority.ALWAYS);

        var emptyState = new EmptyStatePane("No consumer groups found.");
        var groupsStack = new StackPane(groupsList, emptyState);
        emptyState.visibleProperty().bind(Bindings.isEmpty(controller.getGroups()));
        emptyState.managedProperty().bind(emptyState.visibleProperty());
        VBox.setVgrow(groupsStack, Priority.ALWAYS);

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

        var detailsBox = new VBox(10, membersTable, lagTable);
        VBox.setVgrow(detailsBox, Priority.ALWAYS);

        var content = new HBox(10, groupsStack, detailsBox);
        HBox.setHgrow(groupsStack, Priority.ALWAYS);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(viewHeader, progressBar, refreshButton, autoRefresh, content);
        controller.refreshGroups();
    }

}
