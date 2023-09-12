package dev.vepo.kafka.tool;

import dev.vepo.kafka.tool.inspect.ConsumerGroup;
import dev.vepo.kafka.tool.inspect.KafkaAdminService;
import dev.vepo.kafka.tool.inspect.TopicInfo;
import dev.vepo.kafka.tool.stages.TopicSubscribeStage;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

public class ConsumersPane extends VBox {
    private class KafkaConsumerCell extends ListCell<ConsumerGroup> {

        @Override
        protected void updateItem(ConsumerGroup topic, boolean empty) {
            super.updateItem(topic, empty);
            if (Objects.nonNull(topic)) {
                var box = new HBox();

                var topicNameText = new Label(topic.id());
                topicNameText.maxWidth(Double.MAX_VALUE);

                var textBox = new HBox();
                textBox.getChildren().add(topicNameText);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                //var emptyButton = new Button("Empty");
                //emptyButton.setOnAction(event -> {

                //    var alert = new Alert(Alert.AlertType.CONFIRMATION, "All messages will be lost", ButtonType.OK,
                //                         ButtonType.CANCEL);
                //    alert.setTitle("Do you really want to clear the topic?");
                //   alert.show();
                //  alert.resultProperty().addListener((obs, oldValue, newValue) -> {
                //        if (newValue == ButtonType.OK) {
                //          //  adminService.emptyTopic(topic);
                //        }
                //    });
                //});
                //HBox.setHgrow(emptyButton, Priority.ALWAYS);

                //var subscribeButton = new Button("Subscribe");
                //subscribeButton.setOnAction(event -> {
                //    var consumerStage = new TopicSubscribeStage(topic.id(), (Stage) getScene().getWindow(),
                //                                                adminService.connectedBroker());
                //    consumerStage.show();
                //});
                //HBox.setHgrow(subscribeButton, Priority.ALWAYS);

                //var buttonsBox = new HBox(10);
                /// buttonsBox.getChildren().addAll(emptyButton, subscribeButton);
                // HBox.setHgrow(subscribeButton, Priority.ALWAYS);

                box.getChildren().addAll(textBox);//, buttonsBox);
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
    private ListView<ConsumerGroup> listView;

    public ConsumersPane(KafkaAdminService adminService) {
        super();
        this.adminService = adminService;
        this.adminService.watch(state -> {
            if (state == KafkaAdminService.BrokerStatus.CONNECTED) {
                reload();
            } else {
                clear();
            }
        });

        listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(view -> new ConsumersPane.KafkaConsumerCell());

        var refreshButton = new Button("Refresh");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(e -> reload());
        getChildren().addAll(listView, refreshButton);
    }

    public void reload() {
        adminService.listConsumers(topics -> runLater(() -> listView.setItems(observableArrayList(topics))));
    }

    public void clear() {
        runLater(listView.getItems()::clear);
    }
}
