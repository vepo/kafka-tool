package dev.vepo.kafka.tool.stages;

import dev.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import dev.vepo.kafka.tool.controls.builders.ScreenBuilder;
import dev.vepo.kafka.tool.inspect.KafkaAdminService;
import dev.vepo.kafka.tool.settings.WindowSettings;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.stage.Stage;

public class CreateTopicStage extends AbstractKafkaToolStage {

    public CreateTopicStage(Stage owner, KafkaAdminService adminService) {
        super("createTopicStage", owner, true, new WindowSettings(670, 512));
        setTitle("Create Topic");
        var gridBuilder = ScreenBuilder.grid();
        gridBuilder.addText("Name");
        var txtName = gridBuilder.addTextField();
        gridBuilder.newLine().addText("Partitions");
        var partitionsSpinner = gridBuilder.addIntegerField(1, 1024, 1);
        gridBuilder.newLine().addText("Replication Factor");
        var replicationFactorSpinner = gridBuilder.addIntegerField(1, 1024, 1);

        adminService.listBrokers(brokers -> ((IntegerSpinnerValueFactory) replicationFactorSpinner.valueFactoryProperty()
                                                                                                  .get()).setMax(brokers.size()));

        setScene(gridBuilder.build());
    }

}
