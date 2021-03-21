package io.vepo.kt;

import static io.vepo.kt.ui.ResizePolicy.fixedSize;
import static io.vepo.kt.ui.ResizePolicy.grow;
import static java.util.Objects.isNull;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.Optional;

import io.vepo.kt.settings.KafkaBroker;
import io.vepo.kt.settings.Settings;
import io.vepo.kt.ui.ScreenBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Kafka Tool Application
 */
public class KafkaTool extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Platform.exit();
            }
        });
        launch();
    }

    private final KafkaAdminService adminService;

    public KafkaTool() {
        adminService = new KafkaAdminService();
    }

    @Override
    public void start(Stage stage) {
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.setTitle("Kafka Tool");
        var gridBuilder = ScreenBuilder.grid();
        gridBuilder.addText("Servers");
        var serverCombo = gridBuilder.addComboBox(observableArrayList(Settings.kafka().getBrokers()));
        serverCombo.setConverter(new StringConverter<KafkaBroker>() {

            @Override
            public String toString(KafkaBroker broker) {
                return Optional.ofNullable(broker)
                               .map(KafkaBroker::getName)
                               .orElse("Select Kafka Cluster...");
            }

            @Override
            public KafkaBroker fromString(String string) {
                throw new IllegalStateException("Cannot edit");
            }
        });
        serverCombo.setEditable(false);

        var btnConfigureServers = gridBuilder.addButton("Config");
        btnConfigureServers.setOnAction(e -> {
            var configStage = new BrokerConfigurationStage(stage);
            configStage.onCloseRequestProperty()
                       .addListener(__ -> serverCombo.setItems(observableArrayList(Settings.kafka().getBrokers())));
            configStage.show();
        });

        var btnConnect = gridBuilder.newLine().skipCell().addButton("Connect", 2);
        btnConnect.setDisable(true);

        serverCombo.valueProperty()
                   .addListener((obs, oldValue, newValue) -> btnConnect.setDisable(isNull(newValue)));

        var topicsTable = gridBuilder.newLine()
                                     .<TopicInfo>newTableView(3)
                                     .withColumn("Topic")
                                     .fromProperty("name")
                                     .notEditable()
                                     .notResizable()
                                     .resizePolicy(grow(1))
                                     .add()
                                     .withButtons("Actions")
                                     .button("Empty", topic -> {
                                         var alert = new Alert(AlertType.CONFIRMATION, "All messages will be lost",
                                                               ButtonType.OK,
                                                               ButtonType.CANCEL);
                                         alert.setTitle("Do you really want to clear the topic?");
                                         alert.show();
                                         alert.resultProperty()
                                              .addListener((obs, oldValue, newValue) -> {
                                                  if (newValue == ButtonType.OK) {
                                                      adminService.emptyTopic(topic);
                                                  }
                                              });
                                     })
                                     .button("Subscribe", topic -> {
                                         var consumerStage = new TopicSubscribeStage(topic.getName(),
                                                                                     stage,
                                                                                     serverCombo.getValue().clone());
                                         consumerStage.show();
                                     })
                                     .resizePolicy(fixedSize(256))
                                     .add()
                                     .build();

        btnConnect.setOnAction(e -> adminService.connect(serverCombo.getValue(), status -> {
            switch (status) {
                case CONNECTED:
                    adminService.listTopics(topics -> topicsTable.setItems(observableArrayList(topics)));
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected value: " + status);
            }
        }));
//
//        topicsView = new TopicsView(adminService);
//        grid.add(topicsView, 0, 3);
//        setColumnSpan(topicsView, 2);
//        setVgrow(topicsView, Priority.ALWAYS);
//
//        var btnRefreshTopics = new Button("Refresh");
//        btnRefreshTopics.setOnAction(e -> topicsView.update());
//        grid.add(btnRefreshTopics, 0, 4);
//        setColumnSpan(btnRefreshTopics, 2);

//        var currentSettings = Settings.kafka();
//        this.bootstrapField.textProperty().set(currentSettings.bootStrapServers());
//        this.schemaRegistryUrlField.textProperty().set(currentSettings.schemaRegistryUrl());
//        updateButton();

//        var mainWindows = Settings.ui().mainWindow();
//        var scene = new Scene(gridBuilder.build());// mainWindows.width(), mainWindows.height());

        stage.setScene(gridBuilder.build());
        stage.getIcons().add(new Image(KafkaTool.class.getResourceAsStream("/kafka.png")));
        stage.widthProperty().addListener((obs, oldValue, newValue) -> {
//            topicsView.setPrefWidth(newValue.doubleValue());
//            btnClusterConnect.setPrefWidth(grid.getCellBounds(1, 2).getWidth());
//            btnRefreshTopics.setPrefWidth(newValue.doubleValue());

//            var uiSettings = Settings.ui();
//            uiSettings.mainWindow().width(newValue.intValue());
//            uiSettings.save();
        });
//        stage.heightProperty().addListener((obs, oldValue, newValue) -> {
//            var uiSettings = Settings.ui();
//            uiSettings.mainWindow().height(newValue.intValue());
//            uiSettings.save();
//        });
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        this.adminService.close();
    }

}