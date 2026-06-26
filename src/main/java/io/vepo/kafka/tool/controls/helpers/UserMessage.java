package io.vepo.kafka.tool.controls.helpers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public final class UserMessage {

    private static void show(Stage owner, AlertType type, String title, String message) {
        var alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.show();
    }

    public static void showError(Stage owner, String title, String message) {
        show(owner, AlertType.ERROR, title, message);
    }

    public static void showInfo(Stage owner, String title, String message) {
        show(owner, AlertType.INFORMATION, title, message);
    }

    public static void showWarning(Stage owner, String title, String message) {
        show(owner, AlertType.WARNING, title, message);
    }

    private UserMessage() {}

}
