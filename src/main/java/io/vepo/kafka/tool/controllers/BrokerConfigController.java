package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.List;
import java.util.function.Consumer;

import io.vepo.kafka.tool.inspect.ConnectionResult;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.KafkaBrokerValidator;
import io.vepo.kafka.tool.settings.service.SettingsService;
import javafx.collections.ObservableList;

public class BrokerConfigController {

    public static class BrokerValidationException extends RuntimeException {
        public BrokerValidationException(String message) {
            super(message);
        }
    }

    private final SettingsService settingsService;
    private final ApplicationController applicationController;

    private final ObservableList<KafkaBroker> brokers = observableArrayList();

    public BrokerConfigController(SettingsService settingsService, ApplicationController applicationController) {
        this.settingsService = settingsService;
        this.applicationController = applicationController;
        brokers.setAll(settingsService.kafka().getBrokers());
    }

    public void addBroker(KafkaBroker broker) {
        settingsService.updateKafka(kafka -> kafka.getBrokers().add(broker));
        runLater(() -> brokers.add(broker));
    }

    public void applyBrokerEdit(KafkaBroker broker, Runnable applyChange) {
        var snapshot = new KafkaBroker(broker.getName(), broker.getBootStrapServers(), broker.getSchemaRegistryUrl());
        applyChange.run();
        var result = validateBroker(broker);
        if (!result.valid()) {
            broker.setName(snapshot.getName());
            broker.setBootStrapServers(snapshot.getBootStrapServers());
            broker.setSchemaRegistryUrl(snapshot.getSchemaRegistryUrl());
            throw new BrokerValidationException(result.message());
        }
        settingsService.updateKafka(kafka -> kafka.setBrokers(getBackingBrokers()));
    }

    public void deleteBroker(KafkaBroker broker) {
        getBackingBrokers().remove(broker);
        settingsService.updateKafka(kafka -> kafka.getBrokers().remove(broker));
        runLater(() -> brokers.remove(broker));
    }

    public List<KafkaBroker> getBackingBrokers() {
        return settingsService.kafka().getBrokers();
    }

    public ObservableList<KafkaBroker> getBrokers() {
        return brokers;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void testConnection(KafkaBroker broker, Consumer<ConnectionResult> callback) {
        applicationController.testConnection(broker, callback);
    }

    public KafkaBrokerValidator.ValidationResult validateBroker(KafkaBroker broker) {
        return KafkaBrokerValidator.validate(broker, getBackingBrokers());
    }

    public KafkaBrokerValidator.ValidationResult validateDraft(KafkaBroker broker) {
        return KafkaBrokerValidator.validate(broker, getBackingBrokers());
    }

}
