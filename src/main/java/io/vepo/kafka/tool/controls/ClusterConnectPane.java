package io.vepo.kafka.tool.controls;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.Optional;
import java.util.function.Consumer;

import io.vepo.kt.BrokerConfigurationStage;
import io.vepo.kt.settings.KafkaBroker;
import io.vepo.kt.settings.Settings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ClusterConnectPane extends CentralizedPane {

    public ClusterConnectPane(Consumer<KafkaBroker> connectAction) {
        super();
        setMinSize(512, 256);
        setPadding(new Insets(25, 25, 25, 25));

        var selectPane = new GridPane();
        selectPane.setVgap(10);
        selectPane.setHgap(10);

        var txtCluster = new Label("Cluster");
        selectPane.add(txtCluster, 0, 0);

        var cmbCluster = new ComboBox<KafkaBroker>();
        cmbCluster.setItems(observableArrayList(Settings.kafka().getBrokers()));
        cmbCluster.setConverter(new StringConverter<KafkaBroker>() {

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
        cmbCluster.setEditable(false);
        cmbCluster.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(cmbCluster, Priority.ALWAYS);
        GridPane.setFillWidth(cmbCluster, true);
        selectPane.add(cmbCluster, 1, 0);

        var btnConfigure = new Button("Configure");
        btnConfigure.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(btnConfigure, true);
        btnConfigure.setOnAction(e -> {
            var configStage = new BrokerConfigurationStage((Stage) getScene().getWindow());
            configStage.onCloseRequestProperty()
                       .addListener(__ -> cmbCluster.setItems(observableArrayList(Settings.kafka().getBrokers())));
            configStage.show();
        });
        selectPane.add(btnConfigure, 2, 0);

        var btnConnect = new Button("Connect");
        btnConnect.setMaxWidth(Double.MAX_VALUE);
        btnConnect.setOnAction(e -> connectAction.accept(cmbCluster.getValue()));
        GridPane.setFillWidth(btnConnect, true);
        GridPane.setColumnSpan(btnConnect, 2);
        selectPane.add(btnConnect, 1, 1);

        add(selectPane, 512, 256, new Insets(25, 25, 25, 25));
    }

}
