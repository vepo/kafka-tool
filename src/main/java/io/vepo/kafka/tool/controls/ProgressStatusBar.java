package io.vepo.kafka.tool.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class ProgressStatusBar extends HBox {

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty statusText = new SimpleStringProperty("");
    private final ProgressBar progressBar;
    private final Text statusLabel;

    public ProgressStatusBar(double spacing) {
        super(spacing);
        getStyleClass().add("progress-status-bar");
        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        progressBar.setManaged(false);
        HBox.setHgrow(progressBar, javafx.scene.layout.Priority.ALWAYS);
        statusLabel = new Text();
        statusLabel.textProperty().bind(statusText);
        getChildren().addAll(progressBar, statusLabel);
        loading.addListener((obs, oldValue, newValue) -> {
            progressBar.setVisible(newValue);
            progressBar.setManaged(newValue);
            if (newValue) {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            }
        });
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean value) {
        loading.set(value);
    }

    public void setStatusText(String text) {
        statusText.set(text);
    }

    public StringProperty statusTextProperty() {
        return statusText;
    }

}
