package io.vepo.kafka.tool.controls;

import static javafx.geometry.Pos.CENTER_RIGHT;

import io.vepo.kafka.tool.controls.helpers.WindowChromeIcons;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WindowHead extends HBox {
    private Button btnClose;
    private Button btnMaximize;
    private Button btnMinimize;
    private Text title;

    public WindowHead() {
        super();
        getStyleClass().add("window-head");
        setAlignment(CENTER_RIGHT);
        setMaxWidth(Double.MAX_VALUE);

        btnMinimize = chromeButton(WindowChromeIcons.minimize(), this::minimize);
        btnMaximize = chromeButton(WindowChromeIcons.maximize(), this::maximize);
        btnClose = chromeButton(WindowChromeIcons.close(), this::close);
        btnClose.getStyleClass().add("window-close-button");

        title = new Text("");
        title.getStyleClass().addAll("movable", "window-title");
        var titleBox = new HBox();
        titleBox.getStyleClass().add("movable");
        HBox.setMargin(title, new Insets(0, 15, 0, 15));
        titleBox.getChildren().add(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        getChildren().addAll(titleBox, btnMinimize, btnMaximize, btnClose);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    private static Button chromeButton(StackPane icon, EventHandler<ActionEvent> action) {
        var button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("window-chrome-button");
        button.setOnAction(action);
        return button;
    }

    private void maximize(ActionEvent e) {
        var stage = (Stage) getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            btnMaximize.setGraphic(WindowChromeIcons.maximize());
        } else {
            stage.setMaximized(true);
            btnMaximize.setGraphic(WindowChromeIcons.restore());
        }
    }

    private void minimize(ActionEvent e) {
        ((Stage) getScene().getWindow()).setIconified(true);
    }

    private void close(ActionEvent e) {
        Stage window = (Stage) getScene().getWindow();
        window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

}
