package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;
import static io.vepo.kafka.tool.controls.helpers.UserConfirmation.confirm;

import io.vepo.kafka.tool.controllers.TopicsController;
import io.vepo.kafka.tool.controls.EmptyStatePane;
import io.vepo.kafka.tool.controls.ViewActionBar;
import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.controls.builders.ScreenBuilder;
import io.vepo.kafka.tool.inspect.TopicInfo;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TopicsPane extends VBox {

    private static Stage ownerStage(javafx.scene.Node node) {
        return node.getScene() != null ? (Stage) node.getScene().getWindow() : null;
    }

    private final TopicsController controller;

    public TopicsPane(TopicsController controller) {
        super();
        setFillWidth(true);
        this.controller = controller;

        var viewHeader = new ViewHeader(
                                        "Topics",
                                        "Browse, subscribe, or empty topics on the connected cluster.");
        viewHeader.getStyleClass().add("main-window-view-header");
        viewHeader.bindMessage(controller.viewMessage());
        VBox.setVgrow(viewHeader, Priority.NEVER);

        @SuppressWarnings("unchecked")
        TableView<TopicInfo>[] topicsTableRef = new TableView[1];
        topicsTableRef[0] = ScreenBuilder.grid()
                                         .<TopicInfo>addTableView(1)
                                         .withStringColumn("Topic")
                                         .fromProperty(TopicInfo::getName)
                                         .notEditable()
                                         .resizePolicy(grow(1))
                                         .add()
                                         .<String>withColumn("Internal")
                                         .fromProperty(topic -> topic.isInternal() ? "Yes" : "No")
                                         .notEditable()
                                         .resizePolicy(fixedSize(80))
                                         .add()
                                         .withButtons("Actions")
                                         .button("Empty", topic -> {
                                             var owner = ownerStage(topicsTableRef[0]);
                                             if (confirm(owner, "Empty topic?",
                                                         "All messages in \"" + topic.getName() + "\" will be permanently deleted.")) {
                                                 controller.emptyTopic(topic);
                                             }
                                         })
                                         .button("Browse", topic -> controller.openBrowse(
                                                                                          topic.getName(), ownerStage(topicsTableRef[0])))
                                         .button("Subscribe", topic -> controller.openSubscribe(
                                                                                                topic.getName(), ownerStage(topicsTableRef[0])))
                                         .resizePolicy(fixedSize(228))
                                         .add()
                                         .build();
        var topicsTable = topicsTableRef[0];
        topicsTable.setItems(controller.getTopics());
        topicsTable.setMinHeight(120);
        topicsTable.setMaxWidth(Double.MAX_VALUE);

        var emptyState = new EmptyStatePane("No topics found. Click Refresh to load topics from the cluster.");
        var tableStack = new StackPane(topicsTable, emptyState);
        tableStack.setMaxWidth(Double.MAX_VALUE);
        emptyState.visibleProperty().bind(Bindings.isEmpty(controller.getTopics()));
        emptyState.managedProperty().bind(emptyState.visibleProperty());
        VBox.setVgrow(tableStack, Priority.ALWAYS);

        var refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> controller.refreshTopics());

        var disconnectButton = new Button("Disconnect");
        disconnectButton.setOnAction(e -> controller.disconnect());

        var actionBar = new ViewActionBar(refreshButton, disconnectButton);
        actionBar.setMaxHeight(actionBar.prefHeight(-1));
        VBox.setVgrow(actionBar, Priority.NEVER);

        getChildren().addAll(viewHeader, tableStack, actionBar);
    }

}
