package io.vepo.kafka.tool.controllers;

import java.util.List;
import java.util.function.Consumer;

import io.vepo.kafka.tool.inspect.ConnectionResult;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.KafkaBrokerValidator;
import io.vepo.kafka.tool.settings.service.SettingsService;
import javafx.stage.Stage;

public class ClusterConnectController {

    private final SettingsService settingsService;
    private final ApplicationController applicationController;

    public ClusterConnectController(SettingsService settingsService, ApplicationController applicationController) {
        this.settingsService = settingsService;
        this.applicationController = applicationController;
    }

    public void connect(KafkaBroker broker, Consumer<ConnectionResult> callback) {
        applicationController.connect(broker, callback);
    }

    public List<KafkaBroker> getBrokers() {
        return settingsService.kafka().getBrokers();
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void openBrokerConfiguration(Stage owner) {
        applicationController.openBrokerConfiguration(owner);
    }

    public void testConnection(KafkaBroker broker, Consumer<ConnectionResult> callback) {
        applicationController.testConnection(broker, callback);
    }

}
