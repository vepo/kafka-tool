package io.vepo.kafka.tool;

import io.vepo.kafka.tool.controllers.ClusterConnectController;
import io.vepo.kafka.tool.controls.ProgressStatusBar;
import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.settings.KafkaBroker;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Optional;

import static javafx.collections.FXCollections.observableArrayList;

public class ClusterConnectPane extends VBox {

    private static Stage ownerStage(javafx.scene.Node node) {
        return node.getScene() != null ? (Stage) node.getScene().getWindow() : null;
    }

    public ClusterConnectPane(ClusterConnectController controller) {
        super();
        setFillWidth(true);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        var viewHeader = new ViewHeader(
                                        "Connect to Kafka",
                                        "Choose a configured cluster profile. Test the connection before opening the main window.");
        viewHeader.bindMessage(controller.viewMessage());

        var selectPane = new GridPane();
        selectPane.getStyleClass().add("screen-grid");
        selectPane.setVgap(10);
        selectPane.setHgap(10);
        selectPane.setPadding(new Insets(10, 25, 25, 25));

        var progressBar = new ProgressStatusBar(8);
        progressBar.loadingProperty().bind(controller.busyProperty());
        GridPane.setColumnSpan(progressBar, 3);
        selectPane.add(progressBar, 0, 0);

        var txtCluster = new Label("Cluster");
        selectPane.add(txtCluster, 0, 1);

        var cmbCluster = new ComboBox<KafkaBroker>();

        Runnable refreshBrokers = () -> {
            var brokers = controller.getBrokers();
            cmbCluster.setItems(observableArrayList(brokers));
            cmbCluster.setDisable(brokers.isEmpty());
            if (brokers.isEmpty()) {
                controller.viewMessage().showWarning("No clusters configured. Click Configure brokers to add one.");
            } else if (cmbCluster.getValue() == null) {
                cmbCluster.setValue(brokers.get(0));
            }
        };

        refreshBrokers.run();

        cmbCluster.setConverter(new StringConverter<KafkaBroker>() {
            @Override
            public KafkaBroker fromString(String string) {
                throw new IllegalStateException("Cannot edit cluster name here");
            }

            @Override
            public String toString(KafkaBroker broker) {
                return Optional.ofNullable(broker).map(KafkaBroker::getName).orElse("Select Kafka cluster…");
            }
        });
        cmbCluster.setEditable(false);
        cmbCluster.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(cmbCluster, Priority.ALWAYS);
        GridPane.setFillWidth(cmbCluster, true);
        selectPane.add(cmbCluster, 1, 1);

        var btnConfigure = new Button("Configure brokers…");
        btnConfigure.setMaxWidth(Double.MAX_VALUE);
        btnConfigure.setOnAction(e -> {
            controller.openBrokerConfiguration(ownerStage(cmbCluster));
            refreshBrokers.run();
        });
        GridPane.setFillWidth(btnConfigure, true);
        selectPane.add(btnConfigure, 2, 1);

        var btnTest = new Button("Test connection");
        btnTest.setMaxWidth(Double.MAX_VALUE);
        btnTest.disableProperty().bind(cmbCluster.valueProperty().isNull().or(controller.busyProperty()));
        btnTest.setOnAction(e -> controller.testConnection(cmbCluster.getValue(), result -> {}));
        GridPane.setFillWidth(btnTest, true);
        selectPane.add(btnTest, 2, 2);

        var btnConnect = new Button("Connect to cluster");
        btnConnect.setMaxWidth(Double.MAX_VALUE);
        btnConnect.disableProperty().bind(cmbCluster.valueProperty().isNull().or(controller.busyProperty()));
        btnConnect.setOnAction(e -> controller.connect(cmbCluster.getValue(), result -> {}));
        GridPane.setFillWidth(btnConnect, true);
        selectPane.add(btnConnect, 1, 2);

        VBox.setVgrow(selectPane, Priority.ALWAYS);
        getChildren().addAll(viewHeader, selectPane);
    }

}
