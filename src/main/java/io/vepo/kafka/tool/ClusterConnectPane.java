package io.vepo.kafka.tool;

import static io.vepo.kafka.tool.controls.builders.UI.grid;
import static io.vepo.kafka.tool.controls.builders.UI.mainView;
import static io.vepo.kafka.tool.controls.builders.UI.progressBar;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.Optional;

import io.vepo.kafka.tool.controllers.ClusterConnectController;
import io.vepo.kafka.tool.settings.KafkaBroker;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ClusterConnectPane extends VBox {

    private static Stage ownerStage(javafx.scene.Node node) {
        return node.getScene() != null ? (Stage) node.getScene().getWindow() : null;
    }

    public ClusterConnectPane(ClusterConnectController controller) {
        super();
        setFillWidth(true);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        var formBuilder = grid();
        var form = formBuilder.getGridPane();
        form.getColumnConstraints().add(new javafx.scene.layout.ColumnConstraints());

        var progress = progressBar(8);
        progress.loadingProperty().bind(controller.busyProperty());
        formBuilder.addCustom(progress, 3);

        formBuilder.newLine();
        formBuilder.addText("Cluster");
        ObservableList<KafkaBroker> clusterItems = observableArrayList();
        var cmbCluster = formBuilder.addComboBox(clusterItems, 1);

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

        var btnConfigure = new Button("Configure brokers…");
        btnConfigure.setMaxWidth(Double.MAX_VALUE);
        btnConfigure.setOnAction(e -> {
            controller.openBrokerConfiguration(ownerStage(cmbCluster));
            refreshBrokers.run();
        });
        GridPane.setFillWidth(btnConfigure, true);
        form.add(btnConfigure, 2, 1);

        formBuilder.newLine();
        formBuilder.skipCell();
        var btnConnect = formBuilder.addButton("Connect to cluster");
        btnConnect.disableProperty().bind(cmbCluster.valueProperty().isNull().or(controller.busyProperty()));
        btnConnect.setOnAction(e -> controller.connect(cmbCluster.getValue(), result -> {}));

        var btnTest = new Button("Test connection");
        btnTest.setMaxWidth(Double.MAX_VALUE);
        btnTest.disableProperty().bind(cmbCluster.valueProperty().isNull().or(controller.busyProperty()));
        btnTest.setOnAction(e -> controller.testConnection(cmbCluster.getValue(), result -> {}));
        GridPane.setFillWidth(btnTest, true);
        form.add(btnTest, 2, 2);

        VBox.setVgrow(form, Priority.ALWAYS);

        var view = mainView().title("Connect to Kafka",
                                    "Choose a configured cluster profile. Test the connection before opening the main window.")
                             .message(controller.viewMessage())
                             .body(form)
                             .build();
        getChildren().setAll(view.getChildren());
    }

}
