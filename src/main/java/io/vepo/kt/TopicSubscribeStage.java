package io.vepo.kt;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.vepo.kt.UiConstants.PADDING;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.vepo.kt.TopicConsumerStatusBar.Status;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TopicSubscribeStage extends Stage {
    private ListView<String> dataView;
    private ObservableList<String> dataSource;
    private ExecutorService consumerExecutor = newSingleThreadExecutor();
    private TopicConsumerStatusBar consumerStatusBar;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Settings settings;
    private String topic;
    private Button btnStart;
    private Button btnStop;
    private Button btnMessagesClear;

    public TopicSubscribeStage(String topic, Stage owner, Settings settings) {
        this.settings = settings;
        this.topic = topic;
        setTitle("Topic: " + topic);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(PADDING);
        grid.setVgap(PADDING);
        grid.setPadding(new Insets(25, 25, 25, 25));

        var btnControlBox = new HBox(PADDING);
        GridPane.isFillWidth(btnControlBox);
        btnStart = new Button("Start");
        btnStart.setOnAction(e -> startConsumer());
        HBox.setHgrow(btnStart, Priority.ALWAYS);
        btnControlBox.getChildren().add(btnStart);

        btnStop = new Button("Stop");
        btnStop.setOnAction(e -> stopConsumer());
        HBox.setHgrow(btnStop, Priority.ALWAYS);
        btnControlBox.getChildren().add(btnStop);
        btnControlBox.widthProperty().addListener((__, oldValue, newValue) -> {
            btnStart.setPrefWidth((newValue.doubleValue() - PADDING) / 2);
            btnStop.setPrefWidth((newValue.doubleValue() - PADDING) / 2);
        });

        grid.add(btnControlBox, 0, 0);

        dataSource = observableArrayList();
        dataView = new ListView<String>();
        dataView.setItems(dataSource);
        grid.add(dataView, 0, 1);
        GridPane.setFillHeight(dataView, true);
        GridPane.setVgrow(dataView, Priority.ALWAYS);

        var messagesControllerBox = new HBox(PADDING);
        btnMessagesClear = new Button("Clear");
        btnMessagesClear.setOnAction(e -> clearMessages());
        messagesControllerBox.getChildren().add(btnMessagesClear);
        messagesControllerBox.widthProperty()
                             .addListener((__, newValue, oldValue) -> btnMessagesClear.setPrefWidth(newValue.doubleValue()));
        grid.add(messagesControllerBox, 0, 2);

        consumerStatusBar = new TopicConsumerStatusBar(PADDING);
        grid.add(consumerStatusBar, 0, 3);

        setScene(new Scene(grid));

        // Specifies the modality for new window.
        initModality(Modality.WINDOW_MODAL);
        widthProperty().addListener((__, oldWidth, newWidth) -> dataView.setPrefWidth(newWidth.doubleValue()));

        // Specifies the owner Window (parent) for new window
        initOwner(owner);
        updateButton();

        // Set position of second window, related to primary window.
        setX(owner.getX() + 200);
        setY(owner.getY() + 100);
        setWidth(owner.getWidth());
        setHeight(owner.getHeight());
        getIcons().add(new Image(KafkaTool.class.getResourceAsStream("/kafka.png")));
        setOnCloseRequest(e -> stopConsumer());
    }

    private void startConsumer() {
        running.set(true);
        consumerStatusBar.status(Status.CONSUMING);
        consumerExecutor.submit(() -> {
            Properties configProperties = new Properties();
            configProperties.put(BOOTSTRAP_SERVERS_CONFIG, settings.getBootStrapServers());
            configProperties.put(GROUP_ID_CONFIG, "random-" + UUID.randomUUID().toString());
            configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
            configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
            configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, settings.getSchemaRegistryUrl());
            try (KafkaConsumer<String, GenericData.Record> consumer = new KafkaConsumer<>(configProperties)) {
                consumer.subscribe(asList(this.topic));
                while (running.get()) {
                    consumer.poll(Duration.ofSeconds(1))
                            .forEach(record -> runLater(() -> {
                                consumerStatusBar.offset(record.offset());
                                dataSource.add(record.value().toString());
                                dataView.refresh();
                            }));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                consumerStatusBar.status(Status.STOPPED);
            }

        });
        updateButton();
    }

    private void updateButton() {
        btnStart.setDisable(running.get());
        btnStop.setDisable(!running.get());
        btnMessagesClear.setDisable(!running.get());
    }

    private void stopConsumer() {
        running.set(false);
        updateButton();
    }

    private void clearMessages() {
        runLater(() -> {
            dataSource.clear();
            dataView.refresh();
        });
    }
}
