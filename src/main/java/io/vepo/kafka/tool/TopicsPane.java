package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.helpers.UserConfirmation.confirm;

import java.util.Objects;

import io.vepo.kafka.tool.controllers.TopicsController;
import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.inspect.TopicInfo;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TopicsPane extends VBox {

    private class KafkaTopicCell extends ListCell<TopicInfo> {

        @Override
        protected void updateItem(TopicInfo topic, boolean empty) {
            super.updateItem(topic, empty);
            if (Objects.nonNull(topic)) {
                var box = new HBox();

                var topicNameText = new Label(topic.getName());
                topicNameText.maxWidth(Double.MAX_VALUE);

                var textBox = new HBox();
                textBox.getChildren().add(topicNameText);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                var emptyButton = new Button("Empty");
                emptyButton.setOnAction(event -> {
                    var owner = (Stage) getScene().getWindow();
                    if (confirm(owner, "Empty topic?",
                                "All messages in \"" + topic.getName() + "\" will be permanently deleted.")) {
                        controller.emptyTopic(topic);
                    }
                });

                var browseButton = new Button("Browse");
                browseButton.setOnAction(event -> controller.openBrowse(topic.getName(),
                                                                        (Stage) getScene().getWindow()));

                var subscribeButton = new Button("Subscribe");
                subscribeButton.setOnAction(event -> controller.openSubscribe(topic.getName(),
                                                                              (Stage) getScene().getWindow()));

                var buttonsBox = new HBox(10, emptyButton, browseButton, subscribeButton);
                HBox.setHgrow(subscribeButton, Priority.ALWAYS);

                box.getChildren().addAll(textBox, buttonsBox);
                setGraphic(box);
                setText(null);
            } else {
                setText(null);
                setGraphic(null);
                setOnMouseClicked(null);
            }
        }
    }

    private final TopicsController controller;
    private final ListView<TopicInfo> listView;

    public TopicsPane(TopicsController controller) {
        super();
        setFillWidth(true);
        this.controller = controller;

        var viewHeader = new ViewHeader(
                                        "Topics",
                                        "Browse, subscribe, or empty topics on the connected cluster.");
        viewHeader.bindMessage(controller.viewMessage());

        listView = new ListView<>();
        listView.setItems(controller.getTopics());
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(view -> new KafkaTopicCell());

        var refreshButton = new Button("Refresh");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(e -> controller.refreshTopics());

        var disconnectButton = new Button("Disconnect");
        disconnectButton.setOnAction(e -> controller.disconnect());

        var actions = new HBox(10, refreshButton, disconnectButton);
        actions.setFillHeight(true);
        HBox.setHgrow(refreshButton, Priority.ALWAYS);

        VBox.setVgrow(listView, Priority.ALWAYS);
        getChildren().addAll(viewHeader, listView, actions);
    }

}
