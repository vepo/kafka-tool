package io.vepo.kafka.tool.stages;

import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import static io.vepo.kafka.tool.controls.builders.UI.grid;
import io.vepo.kafka.tool.settings.WindowSettings;
import io.vepo.kafka.tool.settings.service.SettingsService;
import javafx.stage.Stage;

public class MessageViewerStage extends AbstractKafkaToolStage {

    public MessageViewerStage(String key, String value, Stage owner, SettingsService settingsService) {
        super("messageViewer", owner, false, new WindowSettings(512, 512), settingsService);
        setTitle("Key: " + key);

        var gridBuilder = grid()
                                .withViewHeader("Message viewer",
                                                "Full message value for the selected record.");
        var viewer = gridBuilder.addTextArea();
        viewer.setText(value);
        viewer.setEditable(false);
        setScene(gridBuilder.build());
    }

}
