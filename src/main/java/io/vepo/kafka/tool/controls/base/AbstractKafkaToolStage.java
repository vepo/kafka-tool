package io.vepo.kafka.tool.controls.base;

import java.util.Objects;
import java.util.Optional;

import io.vepo.kafka.tool.controls.WindowHead;
import io.vepo.kafka.tool.controls.helpers.ResizeHelper;
import io.vepo.kafka.tool.settings.Settings;
import io.vepo.kafka.tool.settings.WindowSettings;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AbstractKafkaToolStage extends Stage {

    protected AbstractKafkaToolStage(String id, Stage owner, boolean modal, WindowSettings defaultSettings) {
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
	widthProperty().addListener((obs, oldValue, newValue) -> Settings.updateUi(
		ui -> ui.getDialogs().computeIfAbsent(id, key -> defaultSettings).setWidth(newValue.intValue())));
	heightProperty().addListener((obs, oldValue, newValue) -> Settings.updateUi(
		ui -> ui.getDialogs().computeIfAbsent(id, key -> defaultSettings).setHeight(newValue.intValue())));
	setup(this);

    }

    private static Optional<WindowHead> findHead(Scene maybeScene) {
	return Optional.ofNullable(maybeScene).map(scene -> scene.getRoot()).map(parent -> findHeadOnChildren(parent));
    }

    private static WindowHead findHeadOnChildren(Parent parent) {
	if (parent instanceof WindowHead) {
	    return (WindowHead) parent;
	} else {
	    return parent.getChildrenUnmodifiable().stream().filter(node -> node instanceof Parent)
		    .map(node -> findHeadOnChildren((Parent) node)).filter(Objects::nonNull).findFirst().orElse(null);
	}
    }

    public static void setup(Stage stage) {
	stage.getIcons().add(new Image(AbstractKafkaToolStage.class.getResourceAsStream("/kafka.png")));
	stage.sceneProperty().addListener((observable, oldScene, newScene) -> {
	    newScene.getStylesheets().add(AbstractKafkaToolStage.class.getResource("/style.css").toExternalForm());
	    findHead(newScene).ifPresent(head -> head.setTitle(stage.getTitle()));
	});
	stage.initStyle(StageStyle.UNDECORATED);
	ResizeHelper.addResizeListener(stage);

	stage.titleProperty().addListener((observable, oldTitle, newTitle) -> findHead(stage.getScene())
		.ifPresent(head -> head.setTitle(newTitle)));
    }
}
