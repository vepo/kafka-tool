package io.vepo.kt;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final String KAFKA_TOOL = ".kafka-tool";
    private static final String KAFKA_PROPERTIES_OBJ = "kafka-properties.obj";
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Platform.exit();
            }
        });
        launch();
    }

    private AtomicBoolean running = new AtomicBoolean(false);
    private TextField bootstrapField;

    private TextField schemaRegistryField;
    private Button btnConsumerStart;
    private Button btnConsumerStop;

    private Button btnClusterConnect;
    private ListView<String> dataView;
    private ObservableList<String> dataSource;
    private ComboBox<String> topicComboBox;
    private ObservableList<String> availableTopics;
    private ExecutorService consumerExecutor = newSingleThreadExecutor();

    private AdminClient adminClient = null;

    private void connect() {
        runLater(() -> btnClusterConnect.setDisable(true));
        var properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapField.textProperty().get().trim());
        adminClient = AdminClient.create(properties);
        adminClient.listTopics().names().whenComplete((names, error) -> {
            if (nonNull(error)) {
                logger.error("Cannot connect with cluster!", error);
                runLater(() -> btnClusterConnect.setDisable(false));
                return;
            }
            saveProperties(true);
            runLater(() -> {
                topicComboBox.setDisable(false);
                availableTopics.clear();
                availableTopics.setAll(names.stream()
                                            .sorted()
                                            .collect(toList()));
                topicComboBox.setItems(availableTopics);
                readProperties(false);
            });
        });
    }

    private Optional<KafkaBrokerProperties> loadProperties() {
        var propertiesFolder = Paths.get(KAFKA_TOOL);
        if (!propertiesFolder.toFile().exists()) {
            propertiesFolder.toFile().mkdir();
        }

        File propertiesFile = propertiesFolder.resolve(KAFKA_PROPERTIES_OBJ).toFile();
        if (propertiesFile.exists()) {
            try (var fis = new FileInputStream(propertiesFile);
                    var ois = new ObjectInputStream(fis)) {
                return Optional.of((KafkaBrokerProperties) ois.readObject());
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Error reading file!", e);
            }
        }
        return Optional.empty();
    }

    private void readProperties(boolean cluster) {
        loadProperties().ifPresent(properties -> {
            if (cluster) {
                bootstrapField.textProperty().set(properties.getBootStrapServers());
            } else {
                schemaRegistryField.textProperty().set(properties.getSchemaRegistryUrl());
                topicComboBox.valueProperty().set(properties.getTopic());
            }
        });
    }

    private void saveProperties(boolean cluster) {
        var properties = loadProperties().orElseGet(KafkaBrokerProperties::new);
        if (cluster) {
            properties.setBootStrapServers(bootstrapField.textProperty().get());
        } else {
            properties.setSchemaRegistryUrl(schemaRegistryField.textProperty().get());
            properties.setTopic(topicComboBox.valueProperty().get());
        }

        var propertiesFolder = Paths.get(KAFKA_TOOL);
        try (var fos = new FileOutputStream(propertiesFolder.resolve(KAFKA_PROPERTIES_OBJ).toFile());
                var oos = new ObjectOutputStream(fos)) {
            oos.writeObject(properties);
            oos.flush();
        } catch (IOException e) {
            logger.error("Error saving file!", e);
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setTitle("Kafka Tool");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        var bootstrapText = new Text("Boostrap Servers");
        bootstrapText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
        grid.add(bootstrapText, 0, 0);

        bootstrapField = new TextField();
        bootstrapField.textProperty().addListener((observable, oldValue, newValue) -> this.updateButton());
        bootstrapField.setMinWidth(256);
        grid.add(bootstrapField, 1, 0);

        btnClusterConnect = new Button("Connect");
        btnClusterConnect.setOnAction(e -> connect());
        btnClusterConnect.setPrefWidth(256);
        grid.add(btnClusterConnect, 1, 1);

        availableTopics = observableArrayList();

        var topicText = new Text("Topic");
        topicText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
        grid.add(topicText, 0, 2);
        topicComboBox = new ComboBox<>();
        topicComboBox.setDisable(true);
        topicComboBox.setMinWidth(256);
        grid.add(topicComboBox, 1, 2);

        var schemaRegistryText = new Text("Schema Registry URL");
        schemaRegistryText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
        grid.add(schemaRegistryText, 0, 3);

        schemaRegistryField = new TextField();
        schemaRegistryField.textProperty()
                           .addListener((observable, oldValue, newValue) -> this.updateButton());
        schemaRegistryField.setMinWidth(256);
        grid.add(schemaRegistryField, 1, 3);

        var buttonsPanel = new HBox(10);
        buttonsPanel.setAlignment(Pos.CENTER_LEFT);
        btnConsumerStart = new Button("Start");
        btnConsumerStart.setPrefWidth(123);
        btnConsumerStart.setOnAction(e -> startConsumer());
        buttonsPanel.getChildren().add(btnConsumerStart);

        btnConsumerStop = new Button("Stop");
        btnConsumerStop.setDisable(true);
        btnConsumerStop.setPrefWidth(123);
        btnConsumerStop.setOnAction(e -> stopConsumer());
        buttonsPanel.getChildren().add(btnConsumerStop);
        grid.add(buttonsPanel, 1, 4);

        dataSource = observableArrayList();
        dataView = new ListView<String>();
        dataView.setItems(dataSource);
        grid.add(dataView, 0, 5);
        GridPane.setColumnSpan(dataView, 2);

        readProperties(true);
        updateButton();
        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private void startConsumer() {
        running.set(true);
        bootstrapField.setEditable(false);
        schemaRegistryField.setEditable(false);

        consumerExecutor.submit(() -> {
            saveProperties(false);
            Properties configProperties = new Properties();
            configProperties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapField.textProperty().get().trim());
            configProperties.put(GROUP_ID_CONFIG, "random-" + UUID.randomUUID().toString());
            configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
            configProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
            configProperties.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryField.textProperty().get().trim());
            try (KafkaConsumer<String, GenericData.Record> consumer = new KafkaConsumer<>(configProperties)) {
                consumer.subscribe(asList(topicComboBox.valueProperty().get()));
                while (running.get()) {
                    consumer.poll(Duration.ofSeconds(1))
                            .forEach(record -> runLater(() -> {
                                dataSource.add(record.value().toString());
                                dataView.refresh();
                            }));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        });
        updateButton();
    }

    @Override
    public void stop() throws Exception {
        running.set(false);
    }

    private void stopConsumer() {
        running.set(false);
        bootstrapField.setEditable(true);
        schemaRegistryField.setEditable(true);
        updateButton();
    }

    private void updateButton() {
        if (nonNull(adminClient)) {
            topicComboBox.setDisable(false);
            schemaRegistryField.setDisable(false);
            btnClusterConnect.setDisable(true);
            if (running.get()) {
                btnConsumerStart.setDisable(true);
                btnConsumerStop.setDisable(false);
            } else {
                if (!(nonNull(topicComboBox.valueProperty().get()) && topicComboBox.valueProperty().get().isEmpty()) &&
                        !schemaRegistryField.textProperty().get().trim().isEmpty()) {
                    btnConsumerStart.setDisable(false);
                } else {
                    btnConsumerStart.setDisable(true);
                }
                btnConsumerStop.setDisable(true);
            }
        } else {
            topicComboBox.setDisable(true);
            schemaRegistryField.setDisable(true);
            btnClusterConnect.setDisable(bootstrapField.textProperty().get().trim().isEmpty());
            btnConsumerStart.setDisable(true);
            btnConsumerStop.setDisable(true);
        }
    }

}