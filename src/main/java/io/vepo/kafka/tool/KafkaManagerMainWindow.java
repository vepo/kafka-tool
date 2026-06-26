package io.vepo.kafka.tool;

import static javafx.application.Platform.runLater;

import io.vepo.kafka.tool.controllers.ApplicationController;
import io.vepo.kafka.tool.controls.MainWindowPane;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import io.vepo.kafka.tool.controls.helpers.ResizeHelper;
import io.vepo.kafka.tool.controls.helpers.WindowHelper;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
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

    @Override
    public void start(Stage stage) throws Exception {
	applicationController = new ApplicationController();
	var settingsService = applicationController.getSettingsService();

	var main = new MainWindowPane();
	var topicsPane = new TopicsPane(applicationController.createTopicsController());
	main.add("Topics", topicsPane);
	main.add("Consumers", new Text("Consumers Pane"));
	stage.initStyle(StageStyle.UNDECORATED);

	AbstractKafkaToolStage.setup(stage);

	var root = WindowHelper.rootControl();
	var clusterConnectController = applicationController.createClusterConnectController();
	var clusterConnectPane = new ClusterConnectPane(clusterConnectController);

	applicationController.setConnectionListener(status -> {
	    if (status == BrokerStatus.CONNECTED) {
		runLater(() -> {
		    var broker = applicationController.getAdminService().connectedBroker();
		    stage.setTitle("Kafka Tool - Connected: " + broker.getName());
		    root.setMain(main);
		});
	    }
	});

	root.setMain(clusterConnectPane);
	stage.setMinWidth(560);
	stage.setMinHeight(360);
	stage.setTitle("Kafka Tool");
	stage.setOnCloseRequest(e -> Platform.exit());

	var scene = new Scene(root, settingsService.ui().getMainWindow().getWidth(),
		settingsService.ui().getMainWindow().getHeight());
	stage.setScene(scene);
	setupUi(stage, scene);
	ResizeHelper.addResizeListener(stage);
	stage.show();
    }

    private void setupUi(Stage stage, Scene scene) {
	scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
	stage.getIcons().add(new Image(KafkaManagerMainWindow.class.getResourceAsStream("/kafka.png")));
	stage.widthProperty().addListener((obs, oldValue, newValue) -> applicationController
		.onMainWindowResize((int) stage.getScene().widthProperty().get(),
			(int) stage.getScene().heightProperty().get()));
	stage.heightProperty().addListener((obs, oldValue, newValue) -> applicationController
		.onMainWindowResize((int) stage.getScene().widthProperty().get(),
			(int) stage.getScene().heightProperty().get()));
    }

    @Override
    public void stop() throws Exception {
	applicationController.shutdown();
	Platform.exit();
	System.exit(0);
    }

}
