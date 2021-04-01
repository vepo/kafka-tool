package io.vepo.kafka.tool;

import static javafx.application.Platform.runLater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.controls.ClusterConnectPane;
import io.vepo.kafka.tool.controls.MainWindowPane;
import io.vepo.kafka.tool.controls.WindowHead;
import io.vepo.kt.KafkaAdminService;
import io.vepo.kt.KafkaAdminService.BrokerStatus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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
        main.add("Topics", new Text("Topics Pane"));
        main.add("Consumers", new Text("Consumers Pane"));
        stage.initStyle(StageStyle.UNDECORATED);

        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: green;");

        var head = new WindowHead();
        borderPane.setTop(head);
        var clusterConnectPane = new ClusterConnectPane(kafkaCluster -> adminService.connect(kafkaCluster, status -> {
            logger.info("Connected? status={}", status);
            if (status == BrokerStatus.CONNECTED) {
                runLater(() -> borderPane.setCenter(main));
            }
        }));
        borderPane.setCenter(clusterConnectPane);

        var scene = new Scene(borderPane);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.onCloseRequestProperty().addListener(e -> adminService.close());
        stage.show();
    }

}
