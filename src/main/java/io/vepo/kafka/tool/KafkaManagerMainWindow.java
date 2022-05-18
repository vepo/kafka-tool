package io.vepo.kafka.tool;

import static javafx.application.Platform.runLater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.controls.MainWindowPane;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import io.vepo.kafka.tool.controls.helpers.ResizeHelper;
import io.vepo.kafka.tool.controls.helpers.WindowHelper;
import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import io.vepo.kafka.tool.settings.Settings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class KafkaManagerMainWindow extends Application {
    private static final Logger logger = LoggerFactory.getLogger(KafkaManagerMainWindow.class);

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Platform.exit();
            }
        });
        launch();
    }

    private KafkaAdminService adminService = new KafkaAdminService();

    @Override
    public void start(Stage stage) throws Exception {
        var main = new MainWindowPane();
        var topicsPane = new TopicsPane(adminService);
        var consumersPane = new ConsumersPane(adminService);
        main.add("Topics", topicsPane);
        main.add("Consumers", consumersPane);
        stage.initStyle(StageStyle.UNDECORATED);

        AbstractKafkaToolStage.setup(stage);

        var root = WindowHelper.rootControl();
        var clusterConnectPane = new ClusterConnectPane(kafkaCluster -> adminService.connect(kafkaCluster, status -> {
            logger.info("Connected? status={}", status);
            if (status == BrokerStatus.CONNECTED) {
                runLater(() -> {
                    stage.setTitle("Kafka Tool - Connected: " + kafkaCluster.getName());
                    root.setMain(main);
                });
            }
        }));
        root.setMain(clusterConnectPane);
        stage.setMinWidth(560);
        stage.setMinHeight(360);
        stage.setTitle("Kafka Tool");

        var scene = new Scene(root, Settings.ui().getMainWindow().getWidth(),
                              Settings.ui().getMainWindow().getHeight());
        stage.setScene(scene);
        setupUi(stage, scene);
        ResizeHelper.addResizeListener(stage);
        stage.show();
    }

    private void setupUi(Stage stage, Scene scene) {
        scene.getStylesheets()
             .add(getClass().getResource("/style.css").toExternalForm());
        stage.getIcons()
             .add(new Image(KafkaManagerMainWindow.class.getResourceAsStream("/kafka.png")));
        stage.widthProperty()
             .addListener((obs, oldValue, newValue) -> Settings
                     .updateUi(ui -> ui.getMainWindow().setWidth((int) stage.getScene().widthProperty().get())));
        stage.heightProperty()
             .addListener((obs, oldValue, newValue) -> Settings
                     .updateUi(ui -> ui.getMainWindow().setHeight((int) stage.getScene().heightProperty().get())));
    }

    @Override
    public void stop() throws Exception {
        adminService.close();
        Platform.exit();
        System.exit(0);
    }

}
