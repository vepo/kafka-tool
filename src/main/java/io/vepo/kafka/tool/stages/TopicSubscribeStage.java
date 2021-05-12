package io.vepo.kafka.tool.stages;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.settings.Settings.serializers;
import static io.vepo.kafka.tool.settings.Settings.updateSerializer;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
import io.vepo.kafka.tool.settings.Entry;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.Serializer;
import io.vepo.kafka.tool.settings.WindowSettings;
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

    private ComboBox<Serializer> cmbSerializer;
    private Function<Serializer, KafkaAgnosticConsumer> serializerFn = s -> {
	if (Serializer.AVRO.equals(s)) {
	    return KafkaAgnosticConsumer.avro();
	} else if (Serializer.JSON.equals(s)) {
	    return KafkaAgnosticConsumer.json();
	} else if (Serializer.PROTOBUF.equals(s)) {
	    return KafkaAgnosticConsumer.protobuf();
	} else {
	    return null;
	}
    };

    public TopicSubscribeStage(String topic, Stage owner, KafkaBroker broker) {
	super("topic-" + topic, owner, false, new WindowSettings(512, 512));
	this.broker = broker;
	this.topic = topic;
	setTitle("Topic: " + topic);

	var gridBuilder = ScreenBuilder.grid();
	gridBuilder.addText("Value Serializer");
	cmbSerializer = gridBuilder
		.addComboBox(observableList(asList(Serializer.AVRO, Serializer.JSON, Serializer.PROTOBUF)), 2);
	serializers().getUsedSerializer().computeIfPresent(topic, (key, value) -> {
	    cmbSerializer.setValue(value);
	    this.selectedConsumer = serializerFn.apply(value);
	    return value;
	});
	cmbSerializer.valueProperty().addListener((obs, oldValue, newValue) -> {
	    updateSerializer(new Entry<>(topic, newValue));
	    this.selectedConsumer = serializerFn.apply(newValue);
	    updateButton();
	});
	cmbSerializer.setConverter(new StringConverter<Serializer>() {

	    @Override
	    public Serializer fromString(String value) {
		return Serializer.valueOf(value);
	    }

	    @Override
	    public String toString(Serializer value) {
		return value == null ? "Select a serializer..." : value.ui();
	    }
	});

	btnStart = gridBuilder.newLine().skipCell().addButton("Start");
        btnStart.setOnAction(e -> startConsumer());
	btnStop = gridBuilder.addButton("Stop");
	btnStop.setOnAction(e -> stopConsumer());

	messageTable = gridBuilder.newLine().<KafkaMessage>addTableView(3).withColumn("Key").fromProperty("key")
		.notEditable().notResizable().notReorderable().resizePolicy(fixedSize(128)).add().withColumn("Message")
		.fromProperty("value").resizable().notEditable().notReorderable().notResizable()
		.resizePolicy(ResizePolicy.grow(1)).add().withButtons("Actions").button("View", message -> {
		    try {
			new MessageViewerStage(message.getKey(), mapper.readTree(message.getValue()).toPrettyString(),
				(Stage) getScene().getWindow()).show();
		    } catch (JsonProcessingException e) {
			logger.error("Could not format JSON!", e);
		    }
		}).resizePolicy(ResizePolicy.fixedSize(128)).add().build();
	messageTable.setDisable(true);
	btnMessagesClear = gridBuilder.newLine().addButton("Clear", 3);
	btnMessagesClear.setOnAction(e -> clearMessages());
	consumerStatusBar = gridBuilder.newLine().addCustom(new TopicConsumerStatusBar(10), 2);
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
		selectedConsumer.start(broker, topic, (metadata, message) -> runLater(() -> {
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
	cmbSerializer.setDisable(true);
    }

    private void stopConsumer() {
	if (nonNull(selectedConsumer)) {
	    selectedConsumer.stop();
	}
	consumerExecutor.submit(() -> runLater(() -> updateButton()));
    }

    private void updateButton() {
	var running = selectedConsumer != null && selectedConsumer.isRunning();
	btnStart.setDisable(selectedConsumer == null || selectedConsumer.isRunning());
	btnStop.setDisable(!running);
	btnMessagesClear.setDisable(!running);
	cmbSerializer.setDisable(running);
    }
}
