package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;

import java.util.List;
import java.util.function.Consumer;

import io.vepo.kafka.tool.inspect.ConnectionResult;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.KafkaBrokerValidator;
import io.vepo.kafka.tool.settings.service.SettingsService;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import io.vepo.kafka.tool.viewmodels.ViewMessageType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

public class ClusterConnectController {

    private final SettingsService settingsService;
    private final ApplicationController applicationController;
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private final ViewMessageModel viewMessage = new ViewMessageModel(
                                                                      "Select a cluster, then test or connect.",
                                                                      ViewMessageType.INFO);

    public ClusterConnectController(SettingsService settingsService, ApplicationController applicationController) {
        this.settingsService = settingsService;
        this.applicationController = applicationController;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public void connect(KafkaBroker broker, Consumer<ConnectionResult> uiFeedback) {
        var bootstrapCheck = KafkaBrokerValidator.validateBootstrapServers(broker.getBootStrapServers());
        if (!bootstrapCheck.valid()) {
            var result = ConnectionResult.failed(bootstrapCheck.message());
            runLater(() -> {
                viewMessage.showError(bootstrapCheck.message());
                uiFeedback.accept(result);
            });
            return;
        }
        busy.set(true);
        viewMessage.showInfo("Connecting to \"" + broker.getName() + "\"…");
        applicationController.connect(broker, result -> {
            busy.set(false);
            if (result.success()) {
                viewMessage.showSuccess(result.message());
            } else {
                viewMessage.showError(result.message());
            }
            uiFeedback.accept(result);
        });
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

    public void testConnection(KafkaBroker broker, Consumer<ConnectionResult> uiFeedback) {
        var bootstrapCheck = KafkaBrokerValidator.validateBootstrapServers(broker.getBootStrapServers());
        if (!bootstrapCheck.valid()) {
            var result = ConnectionResult.failed(bootstrapCheck.message());
            runLater(() -> {
                viewMessage.showError(bootstrapCheck.message());
                uiFeedback.accept(result);
            });
            return;
        }
        busy.set(true);
        viewMessage.showInfo("Testing connection to \"" + broker.getName() + "\"…");
        applicationController.testConnection(broker, result -> {
            busy.set(false);
            if (result.success()) {
                viewMessage.showSuccess(result.message());
            } else {
                viewMessage.showError(result.message());
            }
            uiFeedback.accept(result);
        });
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
