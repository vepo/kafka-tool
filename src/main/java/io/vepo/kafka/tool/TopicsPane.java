package io.vepo.kafka.tool;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.Objects;

import io.vepo.kt.KafkaAdminService;
import io.vepo.kt.KafkaAdminService.BrokerStatus;
import io.vepo.kt.TopicInfo;
import io.vepo.kt.TopicSubscribeStage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class TopicsPane extends ListView<TopicInfo> {

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
		    var consumerStage = new TopicSubscribeStage(topic.getName(), (Stage) getScene().getWindow(),
			    adminService.connectedBroker());
		    consumerStage.show();
		});
		HBox.setHgrow(subscribeButton, Priority.ALWAYS);

		var buttonsBox = new HBox(10);
		buttonsBox.getChildren().addAll(emptyButton, subscribeButton);
		HBox.setHgrow(subscribeButton, Priority.ALWAYS);

		box.getChildren().addAll(textBox, buttonsBox);
		setGraphic(box);
		setText(null);
//		setOnMouseClicked(event -> buttonsBox.getChildrenUnmodifiable().forEach(node -> {
//		    if (node instanceof Button && node.getLayoutBounds().contains(event.getX(), event.getX())) {
//			node.fireEvent(event.copyFor(node, node));
//		    }
//		}));
	    } else {
		setText(null);
		setGraphic(null);
		setOnMouseClicked(null);
	    }

	}

    }

    private KafkaAdminService adminService;

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

	setCellFactory(view -> new KafkaTopicCell());
    }

    public void reload() {
	adminService.listTopics(topics -> runLater(() -> setItems(observableArrayList(topics))));
    }

    public void clear() {
	runLater(() -> getItems().clear());
    }

}
