package io.vepo.kafka.tool.stages;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static javafx.collections.FXCollections.observableList;

import java.util.Objects;

import io.vepo.kafka.tool.controllers.RecordBrowseController;
import io.vepo.kafka.tool.controls.EmptyStatePane;
import io.vepo.kafka.tool.controls.ProgressStatusBar;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import io.vepo.kafka.tool.controls.builders.ResizePolicy;
import io.vepo.kafka.tool.controls.builders.ScreenBuilder;
import io.vepo.kafka.tool.inspect.TopicPartitionInfo;
import io.vepo.kafka.tool.settings.KeySerializer;
import io.vepo.kafka.tool.settings.ValueSerializer;
import io.vepo.kafka.tool.settings.WindowSettings;
import io.vepo.kafka.tool.viewmodels.MessageRow;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class RecordBrowseStage extends AbstractKafkaToolStage {

    private static StringConverter<KeySerializer> keySerializerConverter() {
        return new StringConverter<>() {
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

    private static StringConverter<TopicPartitionInfo> partitionConverter() {
        return new StringConverter<>() {
            @Override
            public TopicPartitionInfo fromString(String string) {
                return null;
            }

            @Override
            public String toString(TopicPartitionInfo partition) {
                return partition == null ? "Select partition..."
                                         : "Partition " + partition.partition() + " [" + partition.beginningOffset()
                                                 + " .. " + (partition.endOffset() - 1) + "]";
            }
        };
    }

    private static StringConverter<ValueSerializer> valueSerializerConverter() {
        return new StringConverter<>() {
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

    private final RecordBrowseController controller;

    public RecordBrowseStage(RecordBrowseController controller, Stage owner) {
        super("browse-" + controller.getTopic(), owner, false, new WindowSettings(720, 560),
              controller.getSettingsService());
        this.controller = controller;
        setTitle("Browse: " + controller.getTopic());

        var gridBuilder = ScreenBuilder.grid()
                                       .withViewHeader("Browse records",
                                                       "Topic \"" + controller.getTopic() + "\". Fetch records by partition and offset.");
        gridBuilder.getViewHeader().bindMessage(controller.viewMessage());

        var progressBar = new ProgressStatusBar(10);
        progressBar.loadingProperty().bind(controller.loadingProperty());
        gridBuilder.addCustom(progressBar, 3);

        gridBuilder.newLine().addText("Partition");
        var cmbPartition = gridBuilder.addComboBox(observableList(controller.getPartitions()), 2);
        cmbPartition.setConverter(partitionConverter());
        cmbPartition.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldValue, newValue) -> controller.setSelectedPartition(newValue));

        gridBuilder.newLine().addText("Start offset");
        var txtOffset = gridBuilder.addTextField();
        txtOffset.setPromptText("Beginning offset");

        gridBuilder.newLine().addText("Max records");
        var spinnerMax = new Spinner<Integer>(1, 500, 50);
        gridBuilder.addCustom(spinnerMax, 1);
        spinnerMax.setMaxWidth(Double.MAX_VALUE);
        spinnerMax.valueProperty().addListener((obs, oldValue, newValue) -> controller.setMaxRecords(newValue.intValue()));

        gridBuilder.newLine().addText("Key Serializer");
        var cmbKeySerializer = gridBuilder.addComboBox(observableList(controller.getKeySerializers()), 2);
        if (controller.getKeySerializer() != null) {
            cmbKeySerializer.setValue(controller.getKeySerializer());
        }
        cmbKeySerializer.setConverter(keySerializerConverter());
        cmbKeySerializer.valueProperty().addListener((obs, oldValue, newValue) -> controller.setKeySerializer(newValue));

        gridBuilder.newLine().addText("Value Serializer");
        var cmbValueSerializer = gridBuilder.addComboBox(observableList(controller.getValueSerializers()), 2);
        if (controller.getValueSerializer() != null) {
            cmbValueSerializer.setValue(controller.getValueSerializer());
        }
        cmbValueSerializer.setConverter(valueSerializerConverter());
        cmbValueSerializer.valueProperty()
                          .addListener((obs, oldValue, newValue) -> controller.setValueSerializer(newValue));

        var btnFetch = gridBuilder.newLine().skipCell().addButton("Fetch");
        btnFetch.setOnAction(e -> {
            try {
                controller.setStartOffset(Long.parseLong(txtOffset.getText().trim()));
            } catch (NumberFormatException ex) {
                controller.viewMessage().showError("Invalid start offset. Enter a numeric offset.");
                return;
            }
            controller.fetchRecords();
        });
        var btnClear = gridBuilder.addButton("Clear");
        btnClear.setOnAction(e -> controller.clearMessages());

        var messageTable = gridBuilder.newLine()
                                      .<MessageRow>addTableView(3)
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
                                                                           .ifPresent(formatted -> new MessageViewerStage(message.getDisplayKey(), formatted,
                                                                                                                          (Stage) getScene().getWindow(),
                                                                                                                          controller.getSettingsService()).show()))
                                      .resizePolicy(fixedSize(96))
                                      .add()
                                      .build();
        messageTable.setItems(controller.getMessages());

        var emptyState = new EmptyStatePane("No records fetched yet.");
        var tableStack = new StackPane(messageTable, emptyState);
        emptyState.visibleProperty().bind(Bindings.isEmpty(controller.getMessages()));
        emptyState.managedProperty().bind(emptyState.visibleProperty());
        gridBuilder.newLine().addCustom(tableStack, 3);

        controller.getPartitions().addListener((javafx.collections.ListChangeListener<TopicPartitionInfo>) change -> {
            if (!controller.getPartitions().isEmpty() && cmbPartition.getValue() == null) {
                cmbPartition.setValue(controller.getPartitions().get(0));
            }
        });

        setScene(gridBuilder.build());
        setOnCloseRequest(e -> controller.shutdown());
    }

}
