package io.vepo.kafka.tool.controllers;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.service.SettingsService;
import io.vepo.kafka.tool.stages.BrokerConfigurationStage;
import io.vepo.kafka.tool.stages.TopicSubscribeStage;
import javafx.stage.Stage;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final KafkaAdminService adminService;
    private final SettingsService settingsService;
    private Consumer<BrokerStatus> connectionListener;

    public ApplicationController() {
	this.adminService = new KafkaAdminService();
	this.settingsService = new SettingsService();
    }

    public KafkaAdminService getAdminService() {
	return adminService;
    }

    public SettingsService getSettingsService() {
	return settingsService;
    }

    public void setConnectionListener(Consumer<BrokerStatus> connectionListener) {
	this.connectionListener = connectionListener;
    }

    public void connect(KafkaBroker broker) {
	adminService.connect(broker, status -> {
	    logger.info("Connected? status={}", status);
	    if (connectionListener != null) {
		connectionListener.accept(status);
	    }
	});
    }

    public void onMainWindowResize(int width, int height) {
	settingsService.updateUi(ui -> {
	    ui.getMainWindow().setWidth(width);
	    ui.getMainWindow().setHeight(height);
	});
    }

    public void shutdown() {
	adminService.close();
    }

    public TopicsController createTopicsController() {
	return new TopicsController(adminService, settingsService, this::openSubscribeStage);
    }

    public ClusterConnectController createClusterConnectController() {
	return new ClusterConnectController(settingsService, this);
    }

    public void openBrokerConfiguration(Stage owner) {
	var controller = new BrokerConfigController(settingsService);
	new BrokerConfigurationStage(controller, owner).showAndWait();
    }

    private void openSubscribeStage(String topic, Stage owner) {
	var subscribeController = new SubscribeController(settingsService, adminService.connectedBroker(), topic);
	new TopicSubscribeStage(subscribeController, owner).show();
    }

}
