package io.vepo.kafka.tool.stages;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static javafx.collections.FXCollections.observableList;

import java.util.Objects;

import io.vepo.kafka.tool.controllers.SubscribeController;
import io.vepo.kafka.tool.controls.EmptyStatePane;
import io.vepo.kafka.tool.controls.TopicConsumerStatusBar;
import io.vepo.kafka.tool.controls.TopicConsumerStatusBar.Status;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import io.vepo.kafka.tool.controls.builders.ResizePolicy;
import io.vepo.kafka.tool.controls.builders.ScreenBuilder;
import io.vepo.kafka.tool.settings.KeySerializer;
import io.vepo.kafka.tool.settings.ValueSerializer;
import io.vepo.kafka.tool.settings.WindowSettings;
import io.vepo.kafka.tool.viewmodels.ConsumerState;
import io.vepo.kafka.tool.viewmodels.MessageRow;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TopicSubscribeStage extends AbstractKafkaToolStage {

    private static StringConverter<KeySerializer> keySerializerConverter() {
        return new StringConverter<KeySerializer>() {
            @Override
            public KeySerializer fromString(String s) {
                return switch (s) {
                    case "Integer" -> KeySerializer.INTEGER;
                    case "String" -> KeySerializer.STRING;
                    default -> null;
                };
            }

            @Override
            public String toString(KeySerializer keySerializer) {
                if (Objects.nonNull(keySerializer)) {
                    return switch (keySerializer) {
                        case INTEGER -> "Integer";
                        case STRING -> "String";
                        default -> "Unknown: " + keySerializer;
                    };
                }
                return "Select a serializer...";
            }
        };
    }

    private static Status mapState(ConsumerState state) {
        return switch (state) {
            case IDLE -> Status.IDLE;
            case RUNNING -> Status.CONSUMING;
            case STOPPED -> Status.STOPPED;
            case ERROR -> Status.ERROR;
        };
    }

    private static StringConverter<ValueSerializer> valueSerializerConverter() {
        return new StringConverter<ValueSerializer>() {
            @Override
            public ValueSerializer fromString(String value) {
                return ValueSerializer.valueOf(value);
            }

            @Override
            public String toString(ValueSerializer value) {
                return value == null ? "Select a serializer..." : value.serializer();
            }
        };
    }

    private final SubscribeController controller;
    private Button btnMessagesClear;
    private Button btnStart;
    private Button btnStop;
    private TopicConsumerStatusBar consumerStatusBar;

    private TableView<MessageRow> messageTable;

    private ComboBox<ValueSerializer> cmbValueSerializer;

    private ComboBox<KeySerializer> cmbKeySerializer;

    public TopicSubscribeStage(SubscribeController controller, Stage owner) {
        super("topic-" + controller.getTopic(), owner, false, new WindowSettings(640, 512),
              controller.getSettingsService());
        this.controller = controller;
        setTitle("Topic: " + controller.getTopic());

        messageTable = ScreenBuilder.grid()
                                    .<MessageRow>addTableView(1)
                                    .<Integer>withColumn("Partition")
                                    .fromProperty(MessageRow::getPartition)
                                    .notEditable()
                                    .resizePolicy(fixedSize(72))
                                    .add()
                                    .<Long>withColumn("Offset")
                                    .fromProperty(MessageRow::getOffset)
                                    .notEditable()
                                    .resizePolicy(fixedSize(96))
                                    .add()
                                    .<Long>withColumn("Timestamp")
                                    .fromProperty(MessageRow::getTimestamp)
                                    .notEditable()
                                    .resizePolicy(fixedSize(112))
                                    .add()
                                    .withColumn("Key")
                                    .fromProperty(MessageRow::getDisplayKey)
                                    .notEditable()
                                    .resizePolicy(fixedSize(128))
                                    .add()
                                    .withColumn("Message")
                                    .fromProperty("displayValue")
                                    .notEditable()
                                    .resizePolicy(ResizePolicy.grow(1))
                                    .add()
                                    .withButtons("Actions")
                                    .button("View", message -> controller.formatValueForViewer(message)
                                                                         .ifPresent(formatted -> new MessageViewerStage(message.getDisplayKey(),
                                                                                                                        formatted,
                                                                                                                        (Stage) getScene().getWindow(),
                                                                                                                        controller.getSettingsService()).show()))
                                    .resizePolicy(fixedSize(72))
                                    .add()
                                    .build();
        messageTable.setDisable(true);
        messageTable.setItems(controller.getMessages());
        messageTable.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        var emptyState = new EmptyStatePane("No messages yet. Press Start to consume.");
        var tableStack = new StackPane(messageTable, emptyState);
        tableStack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        emptyState.visibleProperty().bind(Bindings.isEmpty(controller.getMessages()));
        emptyState.managedProperty().bind(emptyState.visibleProperty());

        var gridBuilder = ScreenBuilder.grid()
                                       .withViewHeader("Subscribe to topic",
                                                       "Topic \"" + controller.getTopic() + "\". Choose serializers and press Start.");
        gridBuilder.getViewHeader().bindMessage(controller.viewMessage());
        gridBuilder.addText("Key Serializer");
        cmbKeySerializer = gridBuilder.addComboBox(observableList(controller.getKeySerializers()), 2);
        if (controller.getKeySerializer() != null) {
            cmbKeySerializer.setValue(controller.getKeySerializer());
        }
        cmbKeySerializer.setConverter(keySerializerConverter());
        cmbKeySerializer.valueProperty().addListener((obs, oldValue, newValue) -> {
            controller.setKeySerializer(newValue);
            updateButtons();
        });

        gridBuilder.newLine().addText("Value Serializer");
        cmbValueSerializer = gridBuilder.addComboBox(observableList(controller.getValueSerializers()), 2);
        if (controller.getValueSerializer() != null) {
            cmbValueSerializer.setValue(controller.getValueSerializer());
        }
        cmbValueSerializer.setConverter(valueSerializerConverter());
        cmbValueSerializer.valueProperty().addListener((obs, oldValue, newValue) -> {
            controller.setValueSerializer(newValue);
            updateButtons();
        });

        btnStart = gridBuilder.newLine().skipCell().addButton("Start");
        btnStart.setOnAction(e -> startConsumer());
        btnStop = gridBuilder.addButton("Stop");
        btnStop.setOnAction(e -> controller.stopConsumer());

        gridBuilder.newLine().addCustom(tableStack, 3);
        GridPane.setHgrow(tableStack, Priority.ALWAYS);
        GridPane.setVgrow(tableStack, Priority.ALWAYS);

        btnMessagesClear = gridBuilder.newLine().addButton("Clear", 3);
        btnMessagesClear.setOnAction(e -> controller.clearMessages());
        consumerStatusBar = gridBuilder.newLine().addCustom(new TopicConsumerStatusBar(10), 2);

        controller.stateProperty().addListener((obs, oldValue, newValue) -> {
            consumerStatusBar.status(mapState(newValue));
            updateButtons();
        });
        controller.offsetProperty().addListener((obs, oldValue, newValue) -> consumerStatusBar.offset(newValue.longValue()));

        setScene(gridBuilder.build());
        updateButtons();
        setOnCloseRequest(e -> controller.shutdown());
    }

    private void startConsumer() {
        messageTable.setDisable(false);
        controller.startConsumer();
        updateButtons();
    }

    private void updateButtons() {
        var running = controller.isRunning();
        btnStart.setDisable(!controller.canStart());
        btnStop.setDisable(!running);
        btnMessagesClear.setDisable(!running);
        cmbValueSerializer.setDisable(running);
    }

}
