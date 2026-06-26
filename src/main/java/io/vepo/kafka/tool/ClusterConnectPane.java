package io.vepo.kafka.tool;

import io.vepo.kafka.tool.controllers.ClusterConnectController;
import io.vepo.kafka.tool.controls.CentralizedPane;
import io.vepo.kafka.tool.controls.helpers.UserMessage;
import io.vepo.kafka.tool.settings.KafkaBroker;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Optional;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

public class ClusterConnectPane extends CentralizedPane {

    public ClusterConnectPane(ClusterConnectController controller) {
        super();
        setMinSize(512, 256);
        setPadding(new Insets(25, 25, 25, 25));

        var selectPane = new GridPane();
        selectPane.getStyleClass().add("screen-grid");
        selectPane.setVgap(10);
        selectPane.setHgap(10);

        var txtCluster = new Label("Cluster");
        selectPane.add(txtCluster, 0, 0);

        var cmbCluster = new ComboBox<KafkaBroker>();

        Runnable updateKafkaOnClustersCombo = () -> {
            var brokers = controller.getBrokers();
            cmbCluster.setItems(observableArrayList(brokers));
            cmbCluster.setDisable(brokers.isEmpty());
        };

        updateKafkaOnClustersCombo.run();

        cmbCluster.setConverter(new StringConverter<KafkaBroker>() {
            @Override
            public KafkaBroker fromString(String string) {
                throw new IllegalStateException("Cannot edit");
            }

            @Override
            public String toString(KafkaBroker broker) {
                return Optional.ofNullable(broker).map(KafkaBroker::getName).orElse("Select Kafka Cluster...");
            }
        });
        cmbCluster.setEditable(false);
        cmbCluster.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(cmbCluster, Priority.ALWAYS);
        GridPane.setFillWidth(cmbCluster, true);
        selectPane.add(cmbCluster, 1, 0);

        var btnConfigure = new Button("Configure");
        btnConfigure.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(btnConfigure, true);
        btnConfigure.setOnAction(e -> {
            controller.openBrokerConfiguration((Stage) getScene().getWindow());
            updateKafkaOnClustersCombo.run();
        });
        selectPane.add(btnConfigure, 2, 0);

        var btnTest = new Button("Test");
        btnTest.setMaxWidth(Double.MAX_VALUE);
        btnTest.disableProperty().bind(cmbCluster.valueProperty().isNull());
        btnTest.setOnAction(e -> controller.testConnection(cmbCluster.getValue(), result -> runLater(() -> {
            if (result.success()) {
                UserMessage.showInfo((Stage) getScene().getWindow(), "Connection test", result.message());
            } else {
                UserMessage.showError((Stage) getScene().getWindow(), "Connection test", result.message());
            }
        })));
        GridPane.setFillWidth(btnTest, true);
        selectPane.add(btnTest, 2, 1);

        var btnConnect = new Button("Connect");
        btnConnect.setMaxWidth(Double.MAX_VALUE);
        btnConnect.disableProperty().bind(cmbCluster.valueProperty().isNull());
        btnConnect.setOnAction(e -> controller.connect(cmbCluster.getValue(), result -> runLater(() -> {
            if (!result.success()) {
                UserMessage.showError((Stage) getScene().getWindow(), "Connection failed", result.message());
            }
        })));
        GridPane.setFillWidth(btnConnect, true);
        GridPane.setColumnSpan(btnConnect, 2);
        selectPane.add(btnConnect, 1, 2);

        add(selectPane, 512, 256, new Insets(25, 25, 25, 25));
    }

}
