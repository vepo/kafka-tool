package io.vepo.kafka.tool.controls;

import static javafx.geometry.Pos.CENTER_RIGHT;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class WindowHead extends HBox {
    public WindowHead() {
        super();
        setAlignment(CENTER_RIGHT);
        setMaxWidth(Double.MAX_VALUE);
        var btnClose = new Button("X");
        btnClose.setOnAction(e -> ((Stage) getScene().getWindow()).close());
        getChildren().add(btnClose);
    }
}
