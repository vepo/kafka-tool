package dev.vepo.kafka.tool;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.kafka.tool.inspect.KafkaAdminService;
import dev.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import dev.vepo.kafka.tool.inspect.TopicInfo;
import dev.vepo.kafka.tool.stages.CreateTopicStage;
import dev.vepo.kafka.tool.stages.TopicSubscribeStage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TopicsPane extends VBox {
    private static final Logger logger = LoggerFactory.getLogger(TopicsPane.class);

    private class KafkaTopicCell extends ListCell<TopicInfo> {

        @Override
        protected void updateItem(TopicInfo topic, boolean empty) {
            super.updateItem(topic, empty);
            if (Objects.nonNull(topic)) {
                var box = new HBox();

                var topicNameText = new Label(topic.name());
                topicNameText.maxWidth(Double.MAX_VALUE);

                var textBox = new HBox();
                textBox.getChildren().add(topicNameText);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                var emptyButton = new Button("Empty");
                emptyButton.setOnAction(event -> {
                    var alert = new Alert(AlertType.CONFIRMATION, "All messages will be lost", ButtonType.OK,
                                          ButtonType.CANCEL);
                    alert.setTitle("Do you really want to clear the topic?");
                    alert.show();
                    alert.resultProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue == ButtonType.OK) {
                            adminService.emptyTopic(topic);
                        }
                    });
                });
                HBox.setHgrow(emptyButton, Priority.ALWAYS);

                var subscribeButton = new Button("Subscribe");
                subscribeButton.setOnAction(event -> {
                    var consumerStage = new TopicSubscribeStage(topic.name(), (Stage) getScene().getWindow(),
                                                                adminService.connectedBroker());
                    consumerStage.show();
                });
                HBox.setHgrow(subscribeButton, Priority.ALWAYS);

                var btnDescribe = new Button("Describe");
                // btnDescribe.setOnAction(event -> {
                // var consumerStage = new TopicSubscribeStage(topic.name(), (Stage)
                // getScene().getWindow(),
                // adminService.connectedBroker());
                // consumerStage.show();
                // });
                HBox.setHgrow(btnDescribe, Priority.ALWAYS);

                var buttonsBox = new HBox(10);
                buttonsBox.getChildren().addAll(emptyButton, subscribeButton, btnDescribe);
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

    private KafkaAdminService adminService;
    private ListView<TopicInfo> listView;

    public TopicsPane(KafkaAdminService adminService) {
        super();
        this.adminService = adminService;
        this.adminService.watch(state -> {
            if (state == BrokerStatus.CONNECTED) {
                reload();
            } else {
                clear();
            }
        });

        listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(view -> new KafkaTopicCell());

        HBox hbButtons = new HBox();

        var refreshButton = new Button("Refresh");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(e -> reload());
        HBox.setHgrow(refreshButton, Priority.ALWAYS);

        var btnNew = new Button("New");
        btnNew.setMaxWidth(Double.MAX_VALUE);
        btnNew.setOnAction(e -> create());
        HBox.setHgrow(btnNew, Priority.ALWAYS);

        hbButtons.getChildren().add(refreshButton);
        hbButtons.getChildren().add(btnNew);
        VBox.setVgrow(hbButtons, Priority.NEVER);

        getChildren().addAll(listView, hbButtons);
    }

    private void create() {
        logger.info("Calling create topic...");
        var createTopicStage = new CreateTopicStage((Stage) getScene().getWindow(), adminService);
        createTopicStage.showAndWait();
    }

    public void reload() {
        logger.info("Calling refresh...");
        adminService.listTopics(topics -> runLater(() -> listView.setItems(observableArrayList(topics))));
    }

    public void clear() {
        runLater(listView.getItems()::clear);
    }

}
