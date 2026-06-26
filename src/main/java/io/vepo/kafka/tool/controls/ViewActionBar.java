package io.vepo.kafka.tool.controls;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Standard action row for main views: refresh on the left, disconnect on the
 * right.
 */
public class ViewActionBar extends HBox {

    public ViewActionBar(Button refreshButton, Button disconnectButton) {
        super(10);
        getStyleClass().add("view-action-bar");
        setPadding(new Insets(10, 0, 20, 0));
        setFillHeight(false);
        setMaxHeight(USE_PREF_SIZE);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(refreshButton, spacer, disconnectButton);
    }

    public ViewActionBar(Button refreshButton, CheckBox autoRefresh, Button disconnectButton) {
        super(10);
        getStyleClass().add("view-action-bar");
        setPadding(new Insets(10, 0, 20, 0));
        setFillHeight(false);
        setMaxHeight(USE_PREF_SIZE);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(refreshButton, autoRefresh, spacer, disconnectButton);
    }

}
