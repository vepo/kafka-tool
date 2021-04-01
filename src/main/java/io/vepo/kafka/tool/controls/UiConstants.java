package io.vepo.kafka.tool.controls;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public interface UiConstants {
    static final Background IDLE_BACKGROUND = new Background(new BackgroundFill(Color.color(0.67, 0.65, 0.71),
                                                                                CornerRadii.EMPTY,
                                                                                new Insets(0, 0, 0,
                                                                                           0)));
    static final Color IDLE_FOREGROUND = Color.BLACK;

    static final Background SELECTED_BACKGROUND = new Background(new BackgroundFill(Color.color(0.31, 0.29, 0.35),
                                                                                    CornerRadii.EMPTY,
                                                                                    new Insets(0, 0, 0,
                                                                                               0)));
    static final Color SELECTED_FOREGROUND = Color.WHITE;

}
