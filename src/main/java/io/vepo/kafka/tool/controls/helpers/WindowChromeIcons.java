package io.vepo.kafka.tool.controls.helpers;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public final class WindowChromeIcons {
    private static final Color ICON_STROKE = Color.web("#FFFFFF");
    private static final double STROKE_WIDTH = 1.25;
    private static final double BUTTON_WIDTH = 46;
    private static final double BUTTON_HEIGHT = 32;

    public static StackPane close() {
        var line1 = new Line(16, 12, 30, 26);
        line1.setStroke(ICON_STROKE);
        line1.setStrokeWidth(STROKE_WIDTH);

        var line2 = new Line(30, 12, 16, 26);
        line2.setStroke(ICON_STROKE);
        line2.setStrokeWidth(STROKE_WIDTH);

        return icon(line1, line2);
    }

    private static StackPane icon(javafx.scene.Node... nodes) {
        var pane = new StackPane(nodes);
        pane.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        pane.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        pane.setMaxSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        pane.setMouseTransparent(true);
        return pane;
    }

    public static StackPane maximize() {
        var frame = new Rectangle(14, 10, 18, 14);
        frame.setFill(Color.TRANSPARENT);
        frame.setStroke(ICON_STROKE);
        frame.setStrokeWidth(STROKE_WIDTH);
        return icon(frame);
    }

    public static StackPane minimize() {
        var line = new Line(14, 20, 32, 20);
        line.setStroke(ICON_STROKE);
        line.setStrokeWidth(STROKE_WIDTH);
        return icon(line);
    }

    public static StackPane restore() {
        var back = new Rectangle(18, 8, 14, 11);
        back.setFill(Color.TRANSPARENT);
        back.setStroke(ICON_STROKE);
        back.setStrokeWidth(STROKE_WIDTH);

        var front = new Rectangle(14, 12, 14, 11);
        front.setFill(Color.TRANSPARENT);
        front.setStroke(ICON_STROKE);
        front.setStrokeWidth(STROKE_WIDTH);

        return icon(back, front);
    }

    private WindowChromeIcons() {}
}
