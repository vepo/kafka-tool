package io.vepo.kafka.tool.controls;

import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import io.vepo.kafka.tool.viewmodels.ViewMessageType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Eclipse-style view header: title, description, and inline status message (no
 * modal dialogs).
 */
public class ViewHeader extends VBox {

    private final Label titleLabel;
    private final Label descriptionLabel;
    private final Label messageLabel;
    private final StringProperty messageText = new SimpleStringProperty("");
    private final ObjectProperty<ViewMessageType> messageType = new SimpleObjectProperty<>(ViewMessageType.NONE);

    public ViewHeader(String title, String description) {
        super(6);
        getStyleClass().add("view-header");
        setFillWidth(true);
        setPadding(new Insets(16, 20, 12, 20));

        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("view-header-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("view-header-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        messageLabel = new Label();
        messageLabel.getStyleClass().add("view-header-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.textProperty().bind(messageText);
        messageLabel.managedProperty().bind(messageLabel.visibleProperty());

        messageType.addListener((obs, oldType, newType) -> applyMessageStyle(newType));
        messageText.addListener((obs, oldText, newText) -> updateMessageVisibility());
        messageType.addListener((obs, oldType, newType) -> updateMessageVisibility());
        updateMessageVisibility();

        getChildren().addAll(titleLabel, descriptionLabel, messageLabel);
    }

    private void applyMessageStyle(ViewMessageType type) {
        messageLabel.getStyleClass().removeAll("info", "success", "warning", "error");
        if (type != null && type != ViewMessageType.NONE) {
            messageLabel.getStyleClass().add(type.name().toLowerCase());
        }
    }

    public void bindMessage(ViewMessageModel model) {
        messageText.bind(model.textProperty());
        messageType.bind(model.typeProperty());
    }

    public StringProperty messageTextProperty() {
        return messageText;
    }

    public ObjectProperty<ViewMessageType> messageTypeProperty() {
        return messageType;
    }

    public void showError(String message) {
        messageType.set(ViewMessageType.ERROR);
        messageText.set(message);
    }

    public void showInfo(String message) {
        messageType.set(ViewMessageType.INFO);
        messageText.set(message);
    }

    public void showSuccess(String message) {
        messageType.set(ViewMessageType.SUCCESS);
        messageText.set(message);
    }

    public void showWarning(String message) {
        messageType.set(ViewMessageType.WARNING);
        messageText.set(message);
    }

    private void updateMessageVisibility() {
        var visible = messageType.get() != ViewMessageType.NONE && !messageText.get().isBlank();
        messageLabel.setVisible(visible);
    }

}
