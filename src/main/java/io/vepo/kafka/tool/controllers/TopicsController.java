package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.function.BiConsumer;

import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import io.vepo.kafka.tool.inspect.TopicInfo;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.service.SettingsService;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class TopicsController {

    private final KafkaAdminService adminService;
    private final SettingsService settingsService;
    private final BiConsumer<String, Stage> subscribeOpener;
    private final ObservableList<TopicInfo> topics = observableArrayList();

    public TopicsController(KafkaAdminService adminService, SettingsService settingsService,
	    BiConsumer<String, Stage> subscribeOpener) {
	this.adminService = adminService;
	this.settingsService = settingsService;
	this.subscribeOpener = subscribeOpener;
	adminService.watch(state -> {
	    if (state == BrokerStatus.CONNECTED) {
		refreshTopics();
	    } else {
		clearTopics();
	    }
	});
    }

    public ObservableList<TopicInfo> getTopics() {
	return topics;
    }

    public void refreshTopics() {
	adminService.listTopics(topicList -> runLater(() -> topics.setAll(topicList)));
    }

    public void clearTopics() {
	runLater(() -> topics.clear());
    }

    public void emptyTopic(TopicInfo topic) {
	adminService.emptyTopic(topic);
    }

    public KafkaBroker getConnectedBroker() {
	return adminService.connectedBroker();
    }

    public void openSubscribe(String topic, Stage owner) {
	subscribeOpener.accept(topic, owner);
    }

    public SettingsService getSettingsService() {
	return settingsService;
    }

}
