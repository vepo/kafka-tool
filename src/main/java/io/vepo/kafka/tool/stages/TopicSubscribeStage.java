package io.vepo.kafka.tool.stages;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.settings.Settings.*;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableList;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.vepo.kafka.tool.settings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.kafka.tool.consumers.AgnosticConsumerException;
import io.vepo.kafka.tool.consumers.KafkaAgnosticConsumer;
import io.vepo.kafka.tool.controls.TopicConsumerStatusBar;
import io.vepo.kafka.tool.controls.TopicConsumerStatusBar.Status;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import io.vepo.kafka.tool.controls.builders.ResizePolicy;
import io.vepo.kafka.tool.controls.builders.ScreenBuilder;
import io.vepo.kafka.tool.inspect.KafkaMessage;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TopicSubscribeStage extends AbstractKafkaToolStage {

    private static final Logger logger = LoggerFactory.getLogger(TopicSubscribeStage.class);

    private Button btnMessagesClear;
    private Button btnStart;
    private Button btnStop;
    private ExecutorService consumerExecutor = newSingleThreadExecutor();
    private TopicConsumerStatusBar consumerStatusBar;
    private TableView<KafkaMessage> messageTable;
    private ObjectMapper mapper = new ObjectMapper();
    private KafkaBroker broker;
    private String topic;
    private KafkaAgnosticConsumer selectedConsumer;

    private ComboBox<ValueSerializer> cmbValueSerializer;

    private ComboBox<KeySerializer> cmbKeySerializer;

    private Function<ValueSerializer, KafkaAgnosticConsumer> serializerValueFn = keySerializer -> {
        if (ValueSerializer.AVRO.equals(keySerializer)) {
            return KafkaAgnosticConsumer.avro();
        } else if (ValueSerializer.JSON.equals(keySerializer)) {
            return KafkaAgnosticConsumer.json();
        } else if (ValueSerializer.PROTOBUF.equals(keySerializer)) {
            return KafkaAgnosticConsumer.protobuf();
        } else {
            return null;
        }
    };

    private static final String byteArray2IntegerString(byte[] key) {
        if (key == null) {
            return "null";
        } else if (key.length != 4) {
            return "Invalid integer";
        } else {
            int value = 0;
            byte[] var4 = key;
            int var5 = key.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                byte b = var4[var6];
                value <<= 8;
                value |= b & 255;
            }

            return Integer.toString(value);
        }
    }

    private Function<byte[], String> serializerKeyFn = key -> {
        return switch (cmbKeySerializer.getValue()) {
            case STRING -> new String(key);
            case INTEGER -> byteArray2IntegerString(key);
            default -> "Not implemented: " + cmbKeySerializer.getValue();
        };
    };

    public TopicSubscribeStage(String topic, Stage owner, KafkaBroker broker) {
        super("topic-" + topic, owner, false, new WindowSettings(512, 512));
        this.broker = broker;
        this.topic = topic;
        setTitle("Topic: " + topic);

        var gridBuilder = ScreenBuilder.grid();
        gridBuilder.addText("Key Serializer");
        cmbKeySerializer = gridBuilder.addComboBox(observableList(asList(KeySerializer.INTEGER, KeySerializer.STRING)), 2);
        serializers().getUsedKeySerializer()
                     .computeIfPresent(topic, (key, value) -> {
                         cmbKeySerializer.setValue(value);
                         return value;
                     });
        cmbKeySerializer.setConverter(new StringConverter<KeySerializer>() {
            @Override
            public String toString(KeySerializer keySerializer) {
                if (Objects.nonNull(keySerializer)) {
                    return switch (keySerializer) {
                        case INTEGER -> "Integer";
                        case STRING -> "String";
                        default -> "Unknown: " + keySerializer;
                    };
                } else {
                    return "Select a serializer...";
                }
            }

            @Override
            public KeySerializer fromString(String s) {
                return switch (s) {
                    case "Integer" -> KeySerializer.INTEGER;
                    case "String" -> KeySerializer.STRING;
                    default -> null;
                };
            }
        });
        cmbKeySerializer.valueProperty()
                        .addListener((obs, oldValue, newValue) -> {
                            updateKeySerializer(new Entry<>(topic, newValue));
                            updateButton();
                        });
        gridBuilder.newLine()
                   .addText("Value Serializer");
        cmbValueSerializer = gridBuilder.addComboBox(observableList(asList(ValueSerializer.AVRO, ValueSerializer.JSON, ValueSerializer.PROTOBUF)), 2);
        serializers().getUsedValueSerializer()
                     .computeIfPresent(topic, (key, value) -> {
                         cmbValueSerializer.setValue(value);
                         this.selectedConsumer = serializerValueFn.apply(value);
                         return value;
                     });
        cmbValueSerializer.valueProperty()
                          .addListener((obs, oldValue, newValue) -> {
                              updateValueSerializer(new Entry<>(topic, newValue));
                              this.selectedConsumer = serializerValueFn.apply(newValue);
                              updateButton();
                          });
        cmbValueSerializer.setConverter(new StringConverter<ValueSerializer>() {

            @Override
            public ValueSerializer fromString(String value) {
                return ValueSerializer.valueOf(value);
            }

            @Override
            public String toString(ValueSerializer value) {
                return value == null ? "Select a serializer..." : value.serializer();
            }
        });

        btnStart = gridBuilder.newLine()
                              .skipCell()
                              .addButton("Start");
        btnStart.setOnAction(e -> startConsumer());
        btnStop = gridBuilder.addButton("Stop");
        btnStop.setOnAction(e -> stopConsumer());

        messageTable = gridBuilder.newLine()
                                  .<KafkaMessage>addTableView(3)
                                  .withColumn("Key")
                                  .fromProperty(value -> serializerKeyFn.apply(value.getKey()))
                                  .notEditable()
                                  .notResizable()
                                  .notReorderable()
                                  .resizePolicy(fixedSize(128))
                                  .add()
                                  .withColumn("Message")
                                  .fromProperty("message")
                                  .resizable()
                                  .notEditable()
                                  .notReorderable()
                                  .notResizable()
                                  .resizePolicy(ResizePolicy.grow(1))
                                  .add()
                                  .withButtons("Actions")
                                  .button("View", message -> {
                                      try {
                                          new MessageViewerStage(serializerKeyFn.apply(message.getKey()),
                                                                 mapper.readTree(message.getValue()).toPrettyString(),
                                                                 (Stage) getScene().getWindow()).show();
                                      } catch (JsonProcessingException e) {
                                          logger.error("Could not format JSON!", e);
                                      }
                                  })
                                  .resizePolicy(ResizePolicy.fixedSize(128))
                                  .add()
                                  .build();
        messageTable.setDisable(true);
        btnMessagesClear = gridBuilder.newLine().addButton("Clear", 3);
        btnMessagesClear.setOnAction(e -> clearMessages());
        consumerStatusBar = gridBuilder.newLine()
                                       .addCustom(new TopicConsumerStatusBar(10), 2);
        setScene(gridBuilder.build());
        updateButton();
        setOnCloseRequest(e -> {
            stopConsumer();
            try {
                consumerExecutor.awaitTermination(2L, TimeUnit.SECONDS);
                consumerExecutor.shutdown();
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void clearMessages() {
        runLater(() -> {
            messageTable.getItems().clear();
        });
    }

    private void startConsumer() {
        clearMessages();
        messageTable.setDisable(false);
        consumerStatusBar.status(Status.CONSUMING);
        consumerExecutor.submit(() -> {
            try {
                selectedConsumer.start(broker,
                                       topic,
                                       (metadata, message) -> runLater(() -> {
                                           consumerStatusBar.offset(metadata.offset());
                                           messageTable.getItems().add(message);
                                       }));
                consumerStatusBar.status(Status.STOPPED);
            } catch (AgnosticConsumerException e) {
                consumerStatusBar.status(Status.ERROR);
                logger.error("Error subscribing to topic!", e);
                updateButton();
            }
        });
        btnStart.setDisable(true);
        btnStop.setDisable(false);
        btnMessagesClear.setDisable(false);
        cmbValueSerializer.setDisable(true);
    }

    private void stopConsumer() {
        if (nonNull(selectedConsumer)) {
            selectedConsumer.stop();
        }
        consumerExecutor.submit(() -> runLater(() -> updateButton()));
    }

    private void updateButton() {
        var running = selectedConsumer != null && cmbKeySerializer.getValue() != null && selectedConsumer.isRunning();
        btnStart.setDisable(selectedConsumer == null || cmbKeySerializer.getValue() == null || selectedConsumer.isRunning());
        btnStop.setDisable(!running);
        btnMessagesClear.setDisable(!running);
        cmbValueSerializer.setDisable(running);
    }
}
