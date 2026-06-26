package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.consumers.AgnosticConsumerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.kafka.tool.consumers.RecordFetcher;
import io.vepo.kafka.tool.consumers.TopicConsumerService;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.inspect.RecordBrowseService;
import io.vepo.kafka.tool.inspect.TopicPartitionInfo;
import io.vepo.kafka.tool.settings.Entry;
import io.vepo.kafka.tool.settings.KeySerializer;
import io.vepo.kafka.tool.settings.ValueSerializer;
import io.vepo.kafka.tool.settings.service.SettingsService;
import io.vepo.kafka.tool.viewmodels.MessageRow;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

public class RecordBrowseController {

    private static final Logger logger = LoggerFactory.getLogger(RecordBrowseController.class);
    private static final int MAX_RECORDS_CAP = 500;

    private final SettingsService settingsService;
    private final RecordBrowseService recordBrowseService;
    private final TopicConsumerService topicConsumerService;
    private final KafkaBroker broker;
    private final String topic;
    private final ObservableList<MessageRow> messages = observableArrayList();
    private final ObservableList<TopicPartitionInfo> partitions = observableArrayList();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final ViewMessageModel viewMessage = new ViewMessageModel();
    private KeySerializer keySerializer;
    private ValueSerializer valueSerializer;
    private final ObjectMapper mapper = new ObjectMapper();
    private TopicPartitionInfo selectedPartition;
    private long startOffset = 0;
    private int maxRecords = 50;

    public RecordBrowseController(SettingsService settingsService, RecordBrowseService recordBrowseService,
                                  KafkaBroker broker, String topic) {
        this.settingsService = settingsService;
        this.recordBrowseService = recordBrowseService;
        this.broker = broker;
        this.topic = topic;
        this.topicConsumerService = new TopicConsumerService();
        loadSavedSerializers();
        loadPartitions();
    }

    public void clearMessages() {
        runLater(() -> messages.clear());
    }

    public void fetchRecords() {
        if (selectedPartition == null || valueSerializer == null || keySerializer == null) {
            viewMessage.showWarning("Select partition and serializers before fetching.");
            return;
        }
        clearMessages();
        runLater(() -> loading.set(true));
        recordBrowseService.fetchRecords(broker, topic, selectedPartition.partition(), startOffset, maxRecords,
                                         valueSerializer, fetched -> runLater(() -> {
                                             for (RecordFetcher.FetchedRecord record : fetched) {
                                                 messages.add(MessageRow.from(record.message(), record.metadata(), keySerializer));
                                             }
                                             loading.set(false);
                                             viewMessage.showSuccess("Fetched " + fetched.size() + " record(s).");
                                         }), error -> runLater(() -> {
                                             loading.set(false);
                                             viewMessage.showError("Fetch failed: " + error.getMessage());
                                             logger.error("Record fetch failed!", error);
                                         }));
    }

    public Optional<String> formatValueForViewer(MessageRow row) {
        try {
            return Optional.of(mapper.readTree(row.getRawValue()).toPrettyString());
        } catch (JsonProcessingException e) {
            logger.error("Could not format JSON!", e);
            return Optional.of(row.getRawValue());
        }
    }

    public KeySerializer getKeySerializer() {
        return keySerializer;
    }

    public List<KeySerializer> getKeySerializers() {
        return List.of(KeySerializer.INTEGER, KeySerializer.STRING);
    }

    public ObservableList<MessageRow> getMessages() {
        return messages;
    }

    public ObservableList<TopicPartitionInfo> getPartitions() {
        return partitions;
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
        return topicConsumerService.availableValueSerializers(broker);
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    private void loadPartitions() {
        runLater(() -> loading.set(true));
        recordBrowseService.describeTopicPartitions(topic, partitionList -> runLater(() -> {
            partitions.setAll(partitionList);
            loading.set(false);
            if (!partitionList.isEmpty()) {
                setSelectedPartition(partitionList.get(0));
            } else {
                viewMessage.showWarning("No partitions found for topic.");
            }
        }));
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

    public void setKeySerializer(KeySerializer keySerializer) {
        this.keySerializer = keySerializer;
        settingsService.updateKeySerializer(new Entry<>(topic, keySerializer));
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = Math.min(Math.max(maxRecords, 1), MAX_RECORDS_CAP);
    }

    public void setSelectedPartition(TopicPartitionInfo partition) {
        this.selectedPartition = partition;
        if (partition != null) {
            startOffset = partition.beginningOffset();
            viewMessage.showInfo("Partition " + partition.partition() + ": offsets " + partition.beginningOffset()
                    + " .. " + (partition.endOffset() - 1));
        }
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public void setValueSerializer(ValueSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
        settingsService.updateValueSerializer(new Entry<>(topic, valueSerializer));
    }

    public void shutdown() {
        recordBrowseService.close();
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
