package io.vepo.kt;

import static io.vepo.kt.UiConstants.PADDING;
import static javafx.scene.layout.GridPane.setColumnSpan;
import static javafx.scene.layout.GridPane.setVgrow;

import java.util.Optional;

import io.vepo.kt.KafkaAdminService.BrokerStatus;
import io.vepo.kt.KafkaAdminService.KafkaConnectionWatcher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Kafka Tool Application
 */
public class KafkaTool extends Application implements KafkaConnectionWatcher {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Platform.exit();
            }
        });
        launch();
    }

    private TextField bootstrapField;

    private Button btnClusterConnect;
    private final KafkaAdminService adminService;

    private GridPane grid;
    private TopicsView topicsView;

    private TextField schemaRegistryUrlField;

    public KafkaTool() {
        adminService = new KafkaAdminService();
        adminService.watch(this);
    }

    private void connect() {
        var boostrapServer = bootstrapField.textProperty().get().trim();
        adminService.connect(boostrapServer, status -> {
            if (status == BrokerStatus.CONNECTED) {
                Settings.readAndUpdate(settings -> {
                    settings.setBootStrapServers(boostrapServer);
                    settings.setSchemaRegistryUrl(Optional.ofNullable(schemaRegistryUrlField.textProperty()
                                                                                            .get())
                                                          .orElse("")
                                                          .trim());
                });
            }
            updateButton();
        });

    }

    private TextField addTextField(String title) {
        int row = grid.getRowCount();

        var textLabel = new Text(title);
        textLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
        grid.add(textLabel, 0, row);

        TextField textField = new TextField();
        textField.textProperty().addListener((observable, oldValue, newValue) -> this.updateButton());
        textField.setMinWidth(256);
        grid.add(textField, 1, row);

        return textField;
    }

    private Button addButton(String label, Runnable callback) {
        int row = grid.getRowCount();

        var btn = new Button(label);
        btn.setOnAction(e -> callback.run());
        btn.setMinWidth(256);
        GridPane.setFillWidth(btn, true);
        grid.add(btn, 1, row);
        grid.widthProperty()
            .addListener((obs, oldValue, newValue) -> btn.setPrefWidth(grid.getCellBounds(1, row).getWidth()));
        return btn;
    }

    @Override
    public void start(Stage stage) {
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.setTitle("Kafka Tool");
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(PADDING);
        grid.setVgap(PADDING);
        grid.setPadding(new Insets(25, 25, 25, 25));

        bootstrapField = addTextField("Boostrap Servers");
        schemaRegistryUrlField = addTextField("Schema Registry URL");
        btnClusterConnect = addButton("Connect", this::connect);

        topicsView = new TopicsView(adminService);
        grid.add(topicsView, 0, 3);
        setColumnSpan(topicsView, 2);
        setVgrow(topicsView, Priority.ALWAYS);

        Settings currentSettings = Settings.getInstance();
        this.bootstrapField.textProperty().set(currentSettings.getBootStrapServers());
        this.schemaRegistryUrlField.textProperty().set(currentSettings.getSchemaRegistryUrl());
        updateButton();
        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.getIcons().add(new Image(KafkaTool.class.getResourceAsStream("/kafka.png")));
        stage.widthProperty().addListener((obs, oldValue, newValue) -> topicsView.setPrefWidth(newValue.doubleValue()));
        stage.show();
    }

    private void updateButton() {
        statusChanged(this.adminService.getStatus());
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void statusChanged(BrokerStatus status) {
        switch (status) {
            case IDLE:
                this.btnClusterConnect.setDisable(Optional.ofNullable(this.bootstrapField.textProperty().get())
                                                          .orElse("")
                                                          .isBlank());
                break;
            case CONNECTED:
                this.btnClusterConnect.setDisable(true);
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + status);
        }
    }

}