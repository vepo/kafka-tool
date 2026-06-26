package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.UI.mainWindow;
import static javafx.application.Platform.runLater;

import io.vepo.kafka.tool.controllers.ApplicationController;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import io.vepo.kafka.tool.controls.helpers.ResizeHelper;
import io.vepo.kafka.tool.controls.helpers.WindowHelper;
import io.vepo.kafka.tool.controls.helpers.WindowHelper.RootControl;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class KafkaManagerMainWindow extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Platform.exit();
            }
        });
        launch();
    }

    private ApplicationController applicationController;
    private RootControl root;
    private ClusterConnectPane clusterConnectPane;
    private Stage primaryStage;

    private void setupUi(Stage stage, Scene scene) {
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.getIcons().add(new Image(KafkaManagerMainWindow.class.getResourceAsStream("/kafka.png")));
        stage.widthProperty().addListener((obs, oldValue, newValue) -> applicationController
                                                                                            .onMainWindowResize((int) stage.getScene().widthProperty().get(),
                                                                                                                (int) stage.getScene().heightProperty().get()));
        stage.heightProperty().addListener((obs, oldValue, newValue) -> applicationController
                                                                                             .onMainWindowResize((int) stage.getScene().widthProperty().get(),
                                                                                                                 (int) stage.getScene().heightProperty()
                                                                                                                            .get()));
    }

    @Override
    public void start(Stage stage) throws Exception {
        applicationController = new ApplicationController();
        var settingsService = applicationController.getSettingsService();
        primaryStage = stage;

        var topicsController = applicationController.createTopicsController();
        var mainRef = new io.vepo.kafka.tool.controls.MainWindowPane[1];
        mainRef[0] = mainWindow()
                                 .tab("Cluster", new ClusterMonitorPane(applicationController.createClusterMonitorController(
                                                                                                                             topic -> applicationController.navigateToTopic(topic,
                                                                                                                                                                            () -> mainRef[0].selectTab("Topics")))))
                                 .tab("Topics", new TopicsPane(topicsController))
                                 .tab("Consumers", new ConsumerGroupsPane(applicationController.createConsumerGroupsController()))
                                 .build();
        var main = mainRef[0];

        stage.initStyle(StageStyle.UNDECORATED);
        AbstractKafkaToolStage.setup(stage);

        root = WindowHelper.rootControl();
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        clusterConnectPane = new ClusterConnectPane(applicationController.createClusterConnectController());
        clusterConnectPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        applicationController.setConnectionListener(status -> {
            if (status == BrokerStatus.CONNECTED) {
                runLater(() -> {
                    var broker = applicationController.getAdminService().connectedBroker();
                    stage.setTitle("Kafka Tool - Connected: " + broker.getName());
                    root.setMain(main);
                });
            }
        });
        applicationController.setDisconnectionListener(() -> runLater(() -> {
            stage.setTitle("Kafka Tool");
            root.setMain(clusterConnectPane);
        }));

        root.setMain(clusterConnectPane);
        stage.setMinWidth(560);
        stage.setMinHeight(360);
        stage.setTitle("Kafka Tool");
        stage.setOnCloseRequest(e -> Platform.exit());

        var scene = new Scene(root, settingsService.ui().getMainWindow().getWidth(),
                              settingsService.ui().getMainWindow().getHeight());
        stage.setScene(scene);
        setupUi(stage, scene);
        ResizeHelper.addResizeListener(stage, stage.getMinWidth(), stage.getMinHeight(), Double.MAX_VALUE,
                                       Double.MAX_VALUE);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        applicationController.shutdown();
        Platform.exit();
        System.exit(0);
    }

}
