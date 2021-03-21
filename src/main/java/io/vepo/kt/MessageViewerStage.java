package io.vepo.kt;

import io.vepo.kt.settings.WindowSettings;
import io.vepo.kt.ui.AbstractKtStage;
import io.vepo.kt.ui.ScreenBuilder;
import javafx.stage.Stage;

public class MessageViewerStage extends AbstractKtStage {

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
