package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;
import static io.vepo.kafka.tool.controls.builders.UI.actionBar;
import static io.vepo.kafka.tool.controls.builders.UI.mainView;
import static io.vepo.kafka.tool.controls.builders.UI.tableWithEmptyState;
import static io.vepo.kafka.tool.controls.helpers.UserConfirmation.confirm;

import io.vepo.kafka.tool.controllers.TopicsController;
import io.vepo.kafka.tool.controls.builders.UI;
import io.vepo.kafka.tool.inspect.TopicInfo;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TopicsPane extends VBox {

    private static Stage ownerStage(javafx.scene.Node node) {
        return node.getScene() != null ? (Stage) node.getScene().getWindow() : null;
    }

    public TopicsPane(TopicsController controller) {
        super();
        setFillWidth(true);

        @SuppressWarnings("unchecked")
        TableView<TopicInfo>[] tableRef = new TableView[1];
        tableRef[0] = UI.<TopicInfo>table().withStringColumn("Topic", TopicInfo::getName, grow(1))
                        .withColumn("Internal", topic -> topic.isInternal() ? "Yes" : "No", fixedSize(80))
                        .withActions("Actions")
                        .button("Empty", topic -> {
                            if (confirm(ownerStage(tableRef[0]), "Empty topic?",
                                        "All messages in \"" + topic.getName() + "\" will be permanently deleted.")) {
                                controller.emptyTopic(topic);
                            }
                        })
                        .button("Browse", topic -> controller.openBrowse(topic.getName(), ownerStage(tableRef[0])))
                        .button("Subscribe",
                                topic -> controller.openSubscribe(topic.getName(), ownerStage(tableRef[0])))
                        .width(fixedSize(228))
                        .add()
                        .items(controller.getTopics())
                        .minHeight(120)
                        .maxWidth(Double.MAX_VALUE)
                        .build();

        var topicsTable = tableRef[0];
        topicsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                controller.selectTopicRequestProperty().set(null);
            }
        });
        controller.selectTopicRequestProperty().addListener((obs, oldValue, topicName) -> {
            if (topicName == null || topicName.isBlank()) {
                return;
            }
            controller.getTopics().stream()
                      .filter(topic -> topicName.equals(topic.getName()))
                      .findFirst()
                      .ifPresent(topic -> topicsTable.getSelectionModel().select(topic));
        });

        var tableStack = tableWithEmptyState(topicsTable, controller.getTopics(),
                                             "No topics found. Click Refresh to load topics from the cluster.");

        var view = mainView().title("Topics", "Browse, subscribe, or empty topics on the connected cluster.")
                             .mainWindowHeader()
                             .message(controller.viewMessage())
                             .body(tableStack)
                             .actionBar(actionBar().refresh("Refresh", controller::refreshTopics)
                                                   .disconnect("Disconnect", controller::disconnect)
                                                   .build())
                             .build();
        getChildren().setAll(view.getChildren());
    }

}
