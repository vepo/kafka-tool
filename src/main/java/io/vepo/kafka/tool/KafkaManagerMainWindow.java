package io.vepo.kafka.tool;

import static javafx.application.Platform.runLater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.controls.MainWindowPane;
import io.vepo.kafka.tool.controls.WindowHead;
import io.vepo.kafka.tool.controls.helpers.ResizeHelper;
import io.vepo.kt.KafkaAdminService;
import io.vepo.kt.KafkaAdminService.BrokerStatus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
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
        main.add("Topics", topicsPane);
        main.add("Consumers", new Text("Consumers Pane"));
        stage.initStyle(StageStyle.UNDECORATED);

        var borderPane = new BorderPane();
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
        stage.getIcons().add(new Image(KafkaManagerMainWindow.class.getResourceAsStream("/kafka.png")));
        stage.setScene(scene);
        ResizeHelper.addResizeListener(stage);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        adminService.close();
        Platform.exit();
        System.exit(0);
    }

}
