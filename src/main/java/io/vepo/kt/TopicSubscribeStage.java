package io.vepo.kt;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.vepo.kt.UiConstants.PADDING;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static javafx.application.Platform.runLater;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.vepo.kt.TopicConsumerStatusBar.Status;
import io.vepo.kt.settings.KafkaSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class TopicSubscribeStage extends Stage {

    public static class Message {
        private final String key;
        private final String value;

        public Message(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Message other = (Message) obj;
            return Objects.equals(key, other.key) && Objects.equals(value, other.value);
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return String.format("Message [key=%s, value=%s]", key, value);
        }

    }

    public class MessagesActionButtonCell extends TableCell<Message, Void> {
        private HBox box;

        public MessagesActionButtonCell() {
            box = new HBox(PADDING);

            var btnAlter = new Button("View");
            btnAlter.setMinWidth(64);
            btnAlter.setOnAction(evnt -> {
                Message message = getTableRow().itemProperty().get();
                if (nonNull(message)) {
                    try {
                        new MessageViewerStage(message.getKey(),
                                               mapper.readTree(message.getValue()).toPrettyString(),
                                               (Stage) getScene().getWindow()).show();
                    } catch (JsonProcessingException e) {
                        logger.error("Could not format JSON!", e);
                    }
                } else {
                    logger.warn("No message!");
                }
            });
            box.getChildren().add(btnAlter);
        }

        @Override
        public void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(box);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TopicSubscribeStage.class);

    private Button btnMessagesClear;
    private Button btnStart;
    private Button btnStop;
    private ExecutorService consumerExecutor = newSingleThreadExecutor();
    private TopicConsumerStatusBar consumerStatusBar;
    private TableView<Message> dataView;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean running = new AtomicBoolean(false);
    private KafkaSettings settings;
    private String topic;

    public TopicSubscribeStage(String topic, Stage owner, KafkaSettings settings) {
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

        dataView = new TableView<>();

        dataView.setEditable(false);
        var keyColumn = new TableColumn<Message, String>("Key");
        keyColumn.setCellValueFactory(new PropertyValueFactory<Message, String>("key"));
        keyColumn.setResizable(false);
        keyColumn.setMaxWidth(128);
        keyColumn.setMinWidth(128);
        dataView.getColumns().add(keyColumn);

        var valueColumn = new TableColumn<Message, String>("Message");
        valueColumn.setCellValueFactory(new PropertyValueFactory<Message, String>("value"));
        valueColumn.setResizable(false);
        valueColumn.setReorderable(false);
        dataView.getColumns().add(valueColumn);

        var actionsColumn = new TableColumn<Message, Void>("Actions");
        actionsColumn.setResizable(false);
        actionsColumn.setReorderable(false);
        actionsColumn.setCellFactory((column) -> new MessagesActionButtonCell());
        actionsColumn.setMaxWidth(PADDING + 64);
        actionsColumn.setMinWidth(PADDING + 64);
        dataView.getColumns().add(actionsColumn);

        valueColumn.setResizable(false);

        dataView.widthProperty().addListener((ov, t, t1) -> {
            valueColumn.setPrefWidth(t1.doubleValue() - (2 * PADDING) - actionsColumn.getWidth()
                    - keyColumn.getWidth());
        });

        grid.add(dataView, 0, 1);
        GridPane.setFillHeight(dataView, true);
        GridPane.setVgrow(dataView, Priority.ALWAYS);

        var messagesControllerBox = new HBox(PADDING);
        btnMessagesClear = new Button("Clear");
        btnMessagesClear.setOnAction(e -> clearMessages());
        messagesControllerBox.getChildren().add(btnMessagesClear);
        messagesControllerBox.widthProperty()
                             .addListener((__, newValue,
                                     oldValue) -> btnMessagesClear.setPrefWidth(newValue.doubleValue()));
        grid.add(messagesControllerBox, 0, 2);

        consumerStatusBar = new TopicConsumerStatusBar(PADDING);
        grid.add(consumerStatusBar, 0, 3);

        setScene(new Scene(grid));

        // Specifies the modality for new window.
        widthProperty().addListener((__, oldWidth, newWidth) -> dataView.setPrefWidth(newWidth.doubleValue()));

        updateButton();

        // Set position of second window, related to primary window.
        setX(owner.getX() + 200);
        setY(owner.getY() + 100);
        setWidth(owner.getWidth());
        setHeight(owner.getHeight());
        owner.onCloseRequestProperty().addListener(e -> this.close());
        getIcons().add(new Image(KafkaTool.class.getResourceAsStream("/kafka.png")));
        setOnCloseRequest(e -> stopConsumer());
    }

    private void clearMessages() {
        runLater(() -> {
            dataView.getItems().clear();
        });
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
                                dataView.getItems().add(new Message(record.key(), record.value().toString()));
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

    private void stopConsumer() {
        running.set(false);
        updateButton();
    }

    private void updateButton() {
        btnStart.setDisable(running.get());
        btnStop.setDisable(!running.get());
        btnMessagesClear.setDisable(!running.get());
    }
}
