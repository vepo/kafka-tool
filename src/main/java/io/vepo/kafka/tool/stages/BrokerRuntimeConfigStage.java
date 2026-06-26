package io.vepo.kafka.tool.stages;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;

import io.vepo.kafka.tool.controllers.BrokerRuntimeConfigController;
import io.vepo.kafka.tool.controls.EmptyStatePane;
import io.vepo.kafka.tool.controls.ProgressStatusBar;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import static io.vepo.kafka.tool.controls.builders.UI.grid;
import io.vepo.kafka.tool.inspect.BrokerConfigEntry;
import io.vepo.kafka.tool.settings.WindowSettings;
import io.vepo.kafka.tool.settings.service.SettingsService;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BrokerRuntimeConfigStage extends AbstractKafkaToolStage {

    public BrokerRuntimeConfigStage(BrokerRuntimeConfigController controller, SettingsService settingsService,
                                    Stage owner) {
        super("broker-runtime-config", owner, false, new WindowSettings(720, 520), settingsService);
        setTitle("Broker " + controller.brokerId() + " configuration");

        var gridBuilder = grid()
                                .withViewHeader("Broker configuration",
                                                "Read-only runtime configuration for broker "
                                                        + controller.brokerId() + ".");
        gridBuilder.getViewHeader().bindMessage(controller.viewMessage());

        var progressBar = new ProgressStatusBar(10);
        progressBar.loadingProperty().bind(controller.loadingProperty());
        gridBuilder.addCustom(progressBar, 2);

        var configTable = gridBuilder.newLine()
                                     .<BrokerConfigEntry>addTableView(2)
                                     .<String>withColumn("Name")
                                     .fromProperty(BrokerConfigEntry::name)
                                     .notEditable()
                                     .resizePolicy(grow(1))
                                     .add()
                                     .<String>withColumn("Value")
                                     .fromProperty(BrokerConfigEntry::displayValue)
                                     .notEditable()
                                     .resizePolicy(grow(1))
                                     .add()
                                     .<String>withColumn("Source")
                                     .fromProperty(BrokerConfigEntry::source)
                                     .notEditable()
                                     .resizePolicy(fixedSize(120))
                                     .add()
                                     .build();
        configTable.setItems(controller.getConfigEntries());

        var emptyState = new EmptyStatePane("No configuration loaded.");
        var tableStack = new StackPane(configTable, emptyState);
        emptyState.visibleProperty().bind(Bindings.isEmpty(controller.getConfigEntries()));
        emptyState.managedProperty().bind(emptyState.visibleProperty());
        gridBuilder.newLine().addCustom(tableStack, 2);

        var refreshButton = gridBuilder.newLine().skipCell().addButton("Refresh");
        refreshButton.setOnAction(e -> controller.loadConfig());

        setScene(gridBuilder.build());
        controller.loadConfig();
    }

}
