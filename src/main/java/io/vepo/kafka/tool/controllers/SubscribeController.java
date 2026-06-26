package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.kafka.tool.consumers.AgnosticConsumerException;
import io.vepo.kafka.tool.consumers.TopicConsumerService;
import io.vepo.kafka.tool.settings.Entry;
import io.vepo.kafka.tool.settings.KeySerializer;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;
import io.vepo.kafka.tool.settings.service.SettingsService;
import io.vepo.kafka.tool.viewmodels.ConsumerState;
import io.vepo.kafka.tool.viewmodels.MessageRow;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

public class SubscribeController {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeController.class);

    private final SettingsService settingsService;
    private final TopicConsumerService consumerService;
    private final KafkaBroker broker;
    private final String topic;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObservableList<MessageRow> messages = observableArrayList();
    private final ObjectProperty<ConsumerState> state = new SimpleObjectProperty<>(ConsumerState.IDLE);
    private final LongProperty offset = new SimpleLongProperty(0);
    private final ViewMessageModel viewMessage = new ViewMessageModel();
    private KeySerializer keySerializer;
    private ValueSerializer valueSerializer;

    public SubscribeController(SettingsService settingsService, KafkaBroker broker, String topic) {
        this.settingsService = settingsService;
        this.broker = broker;
        this.topic = topic;
        this.consumerService = new TopicConsumerService();
        loadSavedSerializers();
    }

    public boolean canStart() {
        return valueSerializer != null && keySerializer != null && state.get() != ConsumerState.RUNNING;
    }

    public void clearMessages() {
        runLater(() -> messages.clear());
    }

    public Optional<String> formatValueForViewer(MessageRow row) {
        try {
            return Optional.of(mapper.readTree(row.getRawValue()).toPrettyString());
        } catch (JsonProcessingException e) {
            logger.error("Could not format JSON!", e);
            return Optional.empty();
        }
    }

    public KeySerializer getKeySerializer() {
        return keySerializer;
    }

    public List<KeySerializer> getKeySerializers() {
        return Arrays.asList(KeySerializer.INTEGER, KeySerializer.STRING);
    }

    public ObservableList<MessageRow> getMessages() {
        return messages;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public String getTopic() {
        return topic;
    }

    public ValueSerializer getValueSerializer() {
        return valueSerializer;
    }

    public List<ValueSerializer> getValueSerializers() {
        return consumerService.availableValueSerializers(broker);
    }

    public boolean isRunning() {
        return state.get() == ConsumerState.RUNNING || consumerService.isRunning();
    }

    private void loadSavedSerializers() {
        settingsService.serializers().getUsedKeySerializer()
                       .computeIfPresent(topic, (key, value) -> {
                           keySerializer = value;
                           return value;
                       });
        settingsService.serializers().getUsedValueSerializer()
                       .computeIfPresent(topic, (key, value) -> {
                           valueSerializer = value;
                           return value;
                       });
    }

    public LongProperty offsetProperty() {
        return offset;
    }

    public void setKeySerializer(KeySerializer keySerializer) {
        this.keySerializer = keySerializer;
        settingsService.updateKeySerializer(new Entry<>(topic, keySerializer));
    }

    public void setValueSerializer(ValueSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
        settingsService.updateValueSerializer(new Entry<>(topic, valueSerializer));
    }

    public void shutdown() {
        consumerService.close();
    }

    public void startConsumer() {
        clearMessages();
        runLater(() -> state.set(ConsumerState.RUNNING));
        consumerService.start(broker, topic, valueSerializer,
                              (metadata, message) -> runLater(() -> {
                                  offset.set(metadata.offset());
                                  messages.add(MessageRow.from(message, metadata, keySerializer));
                              }),
                              () -> runLater(() -> state.set(ConsumerState.STOPPED)),
                              e -> runLater(() -> {
                                  state.set(ConsumerState.ERROR);
                                  viewMessage.showError("Consumer error: " + e.getMessage());
                                  logger.error("Error subscribing to topic!", e);
                              }));
    }

    public ObjectProperty<ConsumerState> stateProperty() {
        return state;
    }

    public void stopConsumer() {
        consumerService.stop();
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
