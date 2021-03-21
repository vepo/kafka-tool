package io.vepo.kt.ui;

import io.vepo.kt.KafkaTool;
import io.vepo.kt.settings.Settings;
import io.vepo.kt.settings.WindowSettings;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AbstractKtStage extends Stage {

    protected AbstractKtStage(String id, Stage owner, boolean modal, WindowSettings defaultSettings) {
        // Set position of second window, related to primary window.
        if (modal) {
            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);
        }

        setX(owner.getX() + 200);
        setY(owner.getY() + 100);
        var settings = Settings.ui().getDialogs().getOrDefault(id, defaultSettings);
        setWidth(settings.getWidth());
        setHeight(settings.getHeight());
        setMinWidth(defaultSettings.getWidth());
        setMinHeight(defaultSettings.getHeight());
        owner.onCloseRequestProperty().addListener(e -> this.close());
        getIcons().add(new Image(KafkaTool.class.getResourceAsStream("/kafka.png")));
        widthProperty().addListener((obs, oldValue, newValue) -> Settings.updateUi(ui -> ui.getDialogs()
                                                                                           .computeIfAbsent(id,
                                                                                                            key -> defaultSettings)
                                                                                           .setWidth(newValue.intValue())));
        heightProperty().addListener((obs, oldValue, newValue) -> Settings.updateUi(ui -> ui.getDialogs()
                                                                                            .computeIfAbsent(id,
                                                                                                             key -> defaultSettings)
                                                                                            .setHeight(newValue.intValue())));
    }

}
