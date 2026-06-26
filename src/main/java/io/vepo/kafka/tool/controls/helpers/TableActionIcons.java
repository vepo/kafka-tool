package io.vepo.kafka.tool.controls.helpers;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public final class TableActionIcons {

    private static final Color ICON_STROKE = Color.web("#FFFFFF");
    private static final double STROKE_WIDTH = 1.4;
    private static final double ICON_SIZE = 28;

    public static StackPane delete() {
        var line1 = new Line(9, 9, 19, 19);
        line1.setStroke(ICON_STROKE);
        line1.setStrokeWidth(STROKE_WIDTH);

        var line2 = new Line(19, 9, 9, 19);
        line2.setStroke(ICON_STROKE);
        line2.setStrokeWidth(STROKE_WIDTH);

        return icon(line1, line2);
    }

    private static StackPane icon(javafx.scene.Node... nodes) {
        var pane = new StackPane(nodes);
        pane.setMinSize(ICON_SIZE, ICON_SIZE);
        pane.setPrefSize(ICON_SIZE, ICON_SIZE);
        pane.setMaxSize(ICON_SIZE, ICON_SIZE);
        pane.setMouseTransparent(true);
        return pane;
    }

    public static StackPane subscribe() {
        var play = new Polygon(10.0, 8.0, 10.0, 20.0, 22.0, 14.0);
        play.setFill(ICON_STROKE);
        return icon(play);
    }

    public static StackPane view() {
        var eye = new Circle(14, 14, 7);
        eye.setFill(Color.TRANSPARENT);
        eye.setStroke(ICON_STROKE);
        eye.setStrokeWidth(STROKE_WIDTH);

        var pupil = new Circle(14, 14, 2.5);
        pupil.setFill(ICON_STROKE);

        return icon(eye, pupil);
    }

    private TableActionIcons() {}

}
