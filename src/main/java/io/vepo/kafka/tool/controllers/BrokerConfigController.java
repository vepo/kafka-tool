package io.vepo.kafka.tool.controllers;

import static javafx.collections.FXCollections.observableList;

import java.util.List;
import java.util.function.Consumer;

import io.vepo.kafka.tool.inspect.ConnectionResult;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.KafkaBrokerValidator;
import io.vepo.kafka.tool.settings.service.SettingsService;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.collections.ObservableList;

public class BrokerConfigController {

    public static class BrokerValidationException extends RuntimeException {
        public BrokerValidationException(String message) {
            super(message);
        }
    }

    private static void trimBroker(KafkaBroker broker) {
        if (broker.getName() != null) {
            broker.setName(broker.getName().trim());
        }
        if (broker.getBootStrapServers() != null) {
            broker.setBootStrapServers(broker.getBootStrapServers().trim());
        }
        if (broker.getSchemaRegistryUrl() != null) {
            broker.setSchemaRegistryUrl(broker.getSchemaRegistryUrl().trim());
        }
    }

    private final SettingsService settingsService;

    private final ApplicationController applicationController;

    private final ObservableList<KafkaBroker> brokers;

    private final ViewMessageModel viewMessage = new ViewMessageModel();

    public BrokerConfigController(SettingsService settingsService, ApplicationController applicationController) {
        this.settingsService = settingsService;
        this.applicationController = applicationController;
        this.brokers = observableList(settingsService.kafka().getBrokers());
    }

    public void addBroker(KafkaBroker broker) {
        brokers.add(broker);
        settingsService.updateKafka(kafka -> {});
    }

    public void applyBrokerEdit(KafkaBroker broker, Runnable applyChange) {
        var snapshot = new KafkaBroker(broker.getName(), broker.getBootStrapServers(), broker.getSchemaRegistryUrl());
        applyChange.run();
        trimBroker(broker);
        var result = validateBroker(broker);
        if (!result.valid()) {
            broker.setName(snapshot.getName());
            broker.setBootStrapServers(snapshot.getBootStrapServers());
            broker.setSchemaRegistryUrl(snapshot.getSchemaRegistryUrl());
            throw new BrokerValidationException(result.message());
        }
        settingsService.updateKafka(kafka -> {});
    }

    public void deleteBroker(KafkaBroker broker) {
        brokers.remove(broker);
        settingsService.updateKafka(kafka -> {});
    }

    public List<KafkaBroker> getBackingBrokers() {
        return brokers;
    }

    public ObservableList<KafkaBroker> getBrokers() {
        return brokers;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void testConnection(KafkaBroker broker, Consumer<ConnectionResult> callback) {
        var bootstrapCheck = KafkaBrokerValidator.validateBootstrapServers(broker.getBootStrapServers());
        if (!bootstrapCheck.valid()) {
            viewMessage.showError(bootstrapCheck.message());
            callback.accept(ConnectionResult.failed(bootstrapCheck.message()));
            return;
        }
        viewMessage.showInfo("Testing connection to \"" + broker.getName() + "\"…");
        applicationController.testConnection(broker, result -> {
            if (result.success()) {
                viewMessage.showSuccess(result.message());
            } else {
                viewMessage.showError(result.message());
            }
            callback.accept(result);
        });
    }

    public KafkaBrokerValidator.ValidationResult validateBroker(KafkaBroker broker) {
        return KafkaBrokerValidator.validate(broker, brokers);
    }

    public KafkaBrokerValidator.ValidationResult validateDraft(KafkaBroker broker) {
        return KafkaBrokerValidator.validate(broker, brokers);
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
