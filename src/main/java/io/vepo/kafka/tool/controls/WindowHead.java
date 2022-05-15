package io.vepo.kafka.tool.controls;

import static javafx.geometry.Pos.CENTER_RIGHT;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WindowHead extends HBox {
    private static final String MINIMIZED_LABEL = new String(
            new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x97, (byte) 0x95}, StandardCharsets.UTF_8).intern();
    private static final String MAXIMIZED_LABEL = new String(
            new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x97, (byte) 0x96}, StandardCharsets.UTF_8).intern();
    private static final String RESTORE_LABEL = new String(
            new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x97, (byte) 0x97}, StandardCharsets.UTF_8).intern();
    private Button btnClose;
    private Button btnMaximize;
    private Button btnMinimize;
    private Text title;

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

        title = new Text("");
        title.getStyleClass().add("movable");
        var titleBox = new HBox();
        titleBox.getStyleClass().add("movable");
        HBox.setMargin(title, new Insets(0, 15, 0, 15));
        titleBox.getChildren().add(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        getChildren().add(titleBox);
        getChildren().add(btnMinimize);
        getChildren().add(btnMaximize);
        getChildren().add(btnClose);
    }

    public void setTitle(String title) {
        this.title.setText(title);
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
        Stage window = (Stage) getScene().getWindow();
        if (Objects.nonNull(window.getOnCloseRequest())) {
            window.getOnCloseRequest().handle(null);
        }
        window.close();
    }

}
