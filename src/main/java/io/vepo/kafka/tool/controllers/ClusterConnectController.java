package io.vepo.kafka.tool.controllers;

import java.util.List;

import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.service.SettingsService;
import javafx.stage.Stage;

public class ClusterConnectController {

    private final SettingsService settingsService;
    private final ApplicationController applicationController;

    public ClusterConnectController(SettingsService settingsService, ApplicationController applicationController) {
	this.settingsService = settingsService;
	this.applicationController = applicationController;
    }

    public List<KafkaBroker> getBrokers() {
	return settingsService.kafka().getBrokers();
    }

    public void connect(KafkaBroker broker) {
	applicationController.connect(broker);
    }

    public void openBrokerConfiguration(Stage owner) {
	applicationController.openBrokerConfiguration(owner);
    }

    public SettingsService getSettingsService() {
	return settingsService;
    }

}
