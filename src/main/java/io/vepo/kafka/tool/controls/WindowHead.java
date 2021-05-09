package io.vepo.kafka.tool.controls;

import static javafx.geometry.Pos.CENTER_RIGHT;

import java.nio.charset.StandardCharsets;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class WindowHead extends HBox {
    private static final String MINIMIZED_LABEL = new String(new byte[] {
        (byte) 0xF0,
        (byte) 0x9F,
        (byte) 0x97,
        (byte) 0x95 }, StandardCharsets.UTF_8).intern();
    private static final String MAXIMIZED_LABEL = new String(new byte[] {
        (byte) 0xF0,
        (byte) 0x9F,
        (byte) 0x97,
        (byte) 0x96 }, StandardCharsets.UTF_8).intern();
    private static final String RESTORE_LABEL = new String(new byte[] {
        (byte) 0xF0,
        (byte) 0x9F,
        (byte) 0x97,
        (byte) 0x97 }, StandardCharsets.UTF_8).intern();
    private Button btnClose;
    private Button btnMaximize;
    private Button btnMinimize;

    public WindowHead() {
        super();
        setAlignment(CENTER_RIGHT);
        setMaxWidth(Double.MAX_VALUE);
        btnClose = new Button("X");
        btnMaximize = new Button(MAXIMIZED_LABEL);
        btnMinimize = new Button(MINIMIZED_LABEL);
        btnClose.setOnAction(this::close);
        btnMaximize.setOnAction(this::maximize);
        btnMinimize.setOnAction(this::minimize);
        getChildren().add(btnMinimize);
        getChildren().add(btnMaximize);
        getChildren().add(btnClose);
    }

    private void maximize(ActionEvent e) {
        if (((Stage) getScene().getWindow()).isMaximized()) {
            ((Stage) getScene().getWindow()).setMaximized(false);
            btnMaximize.setText(MAXIMIZED_LABEL);
        } else {
            ((Stage) getScene().getWindow()).setMaximized(true);
            btnMaximize.setText(RESTORE_LABEL);
        }
    }

    private void minimize(ActionEvent e) {
        ((Stage) getScene().getWindow()).setIconified(true);
    }

    private void close(ActionEvent e) {
        ((Stage) getScene().getWindow()).close();
    }

}
