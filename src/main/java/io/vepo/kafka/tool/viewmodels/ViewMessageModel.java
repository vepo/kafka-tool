package io.vepo.kafka.tool.viewmodels;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ViewMessageModel {

    private final StringProperty text = new SimpleStringProperty("");
    private final ObjectProperty<ViewMessageType> type = new SimpleObjectProperty<>(ViewMessageType.NONE);

    public ViewMessageModel() {}

    public ViewMessageModel(String initialText, ViewMessageType initialType) {
        text.set(initialText);
        type.set(initialType);
    }

    public void clear() {
        type.set(ViewMessageType.NONE);
        text.set("");
    }

    public void showError(String message) {
        type.set(ViewMessageType.ERROR);
        text.set(message);
    }

    public void showInfo(String message) {
        type.set(ViewMessageType.INFO);
        text.set(message);
    }

    public void showSuccess(String message) {
        type.set(ViewMessageType.SUCCESS);
        text.set(message);
    }

    public void showWarning(String message) {
        type.set(ViewMessageType.WARNING);
        text.set(message);
    }

    public StringProperty textProperty() {
        return text;
    }

    public ObjectProperty<ViewMessageType> typeProperty() {
        return type;
    }

}
