package io.vepo.kafka.tool.controls;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class CentralizedPane extends Pane {

    private static final String WIDTH = "WIDTH";
    private static final Object HEIGHT = "HEIGHT";
    private static final Object MARGIN = "MARGIN";

    public CentralizedPane() {
    }

    public void add(Node node, double width, double height, Insets margins) {
        node.getProperties().put(WIDTH, width);
        node.getProperties().put(HEIGHT, height);
        node.getProperties().put(MARGIN, margins);
        getChildren().add(node);
    }

    @Override
    protected void layoutChildren() {
        final double width = getWidth();
        final double height = getHeight();
        double usedHeight = getChildren().stream()
                                         .filter(node -> node.getProperties().containsKey(HEIGHT))
                                         .mapToDouble(this::totalHeight)
                                         .sum();
        double currentTop = (height - usedHeight) / 2;
        for (int index = 0; index < getChildren().size(); ++index) {
            Node child = getChildren().get(index);
            double childHeight = (double) child.getProperties().get(HEIGHT);
            double childWidth = (double) child.getProperties().get(WIDTH);
            double childRealHeight = child.minHeight(childWidth);
            Insets childMargin = (Insets) child.getProperties().get(MARGIN);
            layoutInArea(child,
                         ((width - childWidth) / 2),
                         currentTop + ((childHeight - childRealHeight) / 2),
                         childWidth,
                         childHeight,
                         0.0,
                         childMargin,
                         true,
                         true,
                         HPos.CENTER,
                         VPos.CENTER);
            currentTop += totalHeight(child);
        }
    }

    private double totalHeight(Node node) {
        return (double) node.getProperties().get(HEIGHT) +
                ((Insets) node.getProperties().get(MARGIN)).getTop() +
                ((Insets) node.getProperties().get(MARGIN)).getBottom();
    }

    private double totalWidth(Node node) {
        return (double) node.getProperties().get(WIDTH) +
                ((Insets) node.getProperties().get(MARGIN)).getLeft() +
                ((Insets) node.getProperties().get(MARGIN)).getRight();
    }

    @Override
    protected double computeMinWidth(double height) {
        return super.computeMinWidth(height) + getChildren().stream()
                                                            .filter(node -> node.getProperties().containsKey(HEIGHT))
                                                            .mapToDouble(this::totalWidth)
                                                            .max()
                                                            .orElse(0.0);
    }

    @Override
    protected double computeMinHeight(double width) {
        return super.computeMinHeight(width) + getChildren().stream()
                                                            .filter(node -> node.getProperties().containsKey(HEIGHT))
                                                            .mapToDouble(this::totalHeight)
                                                            .sum();
    }
}
