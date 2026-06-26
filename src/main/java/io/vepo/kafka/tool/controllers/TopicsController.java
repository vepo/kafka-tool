package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.function.BiConsumer;

import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import io.vepo.kafka.tool.inspect.TopicInfo;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.service.SettingsService;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class TopicsController {

    private final KafkaAdminService adminService;
    private final SettingsService settingsService;
    private final BiConsumer<String, Stage> subscribeOpener;
    private final BiConsumer<String, Stage> browseOpener;
    private final Runnable disconnectAction;
    private final ObservableList<TopicInfo> topics = observableArrayList();
    private final ViewMessageModel viewMessage = new ViewMessageModel();

    public TopicsController(KafkaAdminService adminService, SettingsService settingsService,
                            BiConsumer<String, Stage> subscribeOpener, BiConsumer<String, Stage> browseOpener,
                            Runnable disconnectAction) {
        this.adminService = adminService;
        this.settingsService = settingsService;
        this.subscribeOpener = subscribeOpener;
        this.browseOpener = browseOpener;
        this.disconnectAction = disconnectAction;
        adminService.watch(state -> {
            if (state == BrokerStatus.CONNECTED) {
                refreshTopics();
            } else {
                clearTopics();
            }
        });
    }

    public void clearTopics() {
        runLater(() -> topics.clear());
    }

    public void disconnect() {
        disconnectAction.run();
    }

    public void emptyTopic(TopicInfo topic) {
        viewMessage.showInfo("Emptying topic \"" + topic.getName() + "\"…");
        adminService.emptyTopic(topic);
    }

    public KafkaBroker getConnectedBroker() {
        return adminService.connectedBroker();
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public ObservableList<TopicInfo> getTopics() {
        return topics;
    }

    public void openBrowse(String topic, Stage owner) {
        browseOpener.accept(topic, owner);
    }

    public void openSubscribe(String topic, Stage owner) {
        subscribeOpener.accept(topic, owner);
    }

    public void refreshTopics() {
        viewMessage.showInfo("Loading topics…");
        adminService.listTopics(topicList -> runLater(() -> {
            topics.setAll(topicList);
            viewMessage.showSuccess("Found " + topicList.size() + " topic(s).");
        }));
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
