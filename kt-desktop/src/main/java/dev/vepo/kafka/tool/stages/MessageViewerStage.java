package dev.vepo.kafka.tool.stages;

import dev.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import dev.vepo.kafka.tool.controls.builders.ScreenBuilder;
import dev.vepo.kafka.tool.settings.WindowSettings;
import javafx.stage.Stage;

public class MessageViewerStage extends AbstractKafkaToolStage {

    public MessageViewerStage(String key, String value, Stage owner) {
        super("messageViewer", owner, false, new WindowSettings(512, 512));
        setTitle("Key: " + key);

        var gridBuilder = ScreenBuilder.grid();
        var viewer = gridBuilder.addTextArea();
        viewer.setText(value);
        viewer.setEditable(false);
        setScene(gridBuilder.build());
    }

}
