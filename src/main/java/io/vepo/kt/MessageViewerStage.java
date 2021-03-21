package io.vepo.kt;

import static io.vepo.kt.UiConstants.PADDING;

import io.vepo.kt.settings.WindowSettings;
import io.vepo.kt.ui.AbstractKtStage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class MessageViewerStage extends AbstractKtStage {

    public MessageViewerStage(String key, String value, Stage owner) {
        super("messageViewer", owner, false, new WindowSettings(512, 512));
        setTitle("Key: " + key);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(PADDING);
        grid.setVgap(PADDING);
        grid.setPadding(new Insets(25, 25, 25, 25));

        TextArea viewer = new TextArea(value);
        viewer.setEditable(false);
        grid.add(viewer, 0, 0);
        GridPane.setHgrow(viewer, Priority.ALWAYS);
        GridPane.setVgrow(viewer, Priority.ALWAYS);
        setScene(new Scene(grid));
    }

}
