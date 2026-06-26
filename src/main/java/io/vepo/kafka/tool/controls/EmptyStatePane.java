package io.vepo.kafka.tool.controls;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EmptyStatePane extends VBox {

    public EmptyStatePane(String message) {
        super(10);
        setAlignment(Pos.CENTER);
        getStyleClass().add("empty-state");
        var label = new Label(message);
        label.getStyleClass().add("empty-state-message");
        getChildren().add(label);
    }

}
