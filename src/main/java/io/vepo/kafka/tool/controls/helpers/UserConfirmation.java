package io.vepo.kafka.tool.controls.helpers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal confirmation for destructive actions only (Eclipse: dialogs for
 * commitment, not status).
 */
public final class UserConfirmation {

    public static boolean confirm(Stage owner, String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("UserConfirmation must be called on the JavaFX application thread");
        }
        return showConfirm(owner, title, message);
    }

    private static boolean showConfirm(Stage owner, String title, String message) {
        var alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            alert.initOwner(owner);
        }
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private UserConfirmation() {}

}
