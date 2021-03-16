package io.vepo.kt;

import static io.vepo.kt.UiConstants.PADDING;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class MessageViewerStage extends Stage {

    public MessageViewerStage(String key, String value, Stage owner) {
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

        // Set position of second window, related to primary window.
        setX(owner.getX() + 200);
        setY(owner.getY() + 100);
        setWidth(owner.getWidth());
        setHeight(owner.getHeight());
        owner.onCloseRequestProperty().addListener(e -> this.close());
        getIcons().add(new Image(KafkaTool.class.getResourceAsStream("/kafka.png")));
    }

}
