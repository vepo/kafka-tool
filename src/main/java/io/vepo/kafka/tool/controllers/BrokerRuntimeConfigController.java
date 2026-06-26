package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import io.vepo.kafka.tool.inspect.BrokerConfigEntry;
import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

public class BrokerRuntimeConfigController {

    private final KafkaAdminService adminService;
    private final int brokerId;
    private final ObservableList<BrokerConfigEntry> configEntries = observableArrayList();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final ViewMessageModel viewMessage = new ViewMessageModel();

    public BrokerRuntimeConfigController(KafkaAdminService adminService, int brokerId) {
        this.adminService = adminService;
        this.brokerId = brokerId;
    }

    public int brokerId() {
        return brokerId;
    }

    public ObservableList<BrokerConfigEntry> getConfigEntries() {
        return configEntries;
    }

    public void loadConfig() {
        runLater(() -> loading.set(true));
        adminService.describeBrokerConfig(brokerId, entries -> runLater(() -> {
            configEntries.setAll(entries);
            loading.set(false);
            if (entries.isEmpty()) {
                viewMessage.showWarning("No configuration entries returned for broker " + brokerId + ".");
            } else {
                viewMessage.showSuccess("Loaded " + entries.size() + " configuration entries.");
            }
        }));
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
