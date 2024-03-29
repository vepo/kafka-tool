package io.vepo.kafka.tool.controls;

import static java.util.stream.Collectors.toList;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class MainWindowPane extends Pane {
    private String POSITION = "position";
    private String VIEWER = "viewer";
    private String SELECTED = "selected";
    private int counter;

    public MainWindowPane() {
        counter = 0;
    }

    public void add(String label, Node viewer) {
        var btn = new Button(label);
        btn.setOnAction(e -> {
            btn.getProperties().put(SELECTED, true);
            btn.getStyleClass().remove(SELECTED);
            btn.getStyleClass().add(SELECTED);
            viewer.getProperties().put(SELECTED, true);
            getChildren().stream()
                         .filter(control -> control != btn && control != viewer)
                         .forEach(node -> {
                             node.getProperties().put(SELECTED, false);
                             node.getStyleClass().remove(SELECTED);
                         });
            requestLayout();
        });

        boolean selected = counter == 0;

        viewer.getProperties().put(POSITION, counter);
        viewer.getProperties().put(VIEWER, true);
        viewer.getProperties().put(SELECTED, selected);

        if (selected) {
            btn.getStyleClass().add(SELECTED);
        }
        btn.getProperties().put(POSITION, counter++);
        btn.getProperties().put(VIEWER, false);
        btn.getProperties().put(SELECTED, selected);
        getChildren().addAll(btn, viewer);
    }

    @Override
    protected void layoutChildren() {
        final double width = getWidth();
        final double height = getHeight();
        var buttons = getChildren().stream()
                                   .filter(control -> !(boolean) control.getProperties().get(VIEWER))
                                   .map(node -> (Button) node)
                                   .sorted((a, b) -> Integer.compare((int) a.getProperties().get(POSITION),
                                                                     (int) b.getProperties().get(POSITION)))
                                   .collect(toList());
        var btnWidth = 120;
        var btnHeight = 50;
        for (var index = 0; index < buttons.size(); index++) {
            var btn = buttons.get(index);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setMaxHeight(Double.MAX_VALUE);
            btn.resize(btnWidth, btnHeight);
            layoutInArea(btn,
                         0,
                         btnHeight * index,
                         btnWidth,
                         btnHeight,
                         0,
                         new Insets(0, 0, 0, 0),
                         true,
                         true,
                         HPos.CENTER,
                         VPos.CENTER);
        }

        getChildren().stream()
                     .filter(control -> ((boolean) control.getProperties().get(VIEWER)))
                     .forEach(control -> control.setVisible((boolean) control.getProperties().get(SELECTED)));

        var viewer = getChildren().stream()
                                  .filter(control -> ((boolean) control.getProperties().get(VIEWER))
                                          && ((boolean) control.getProperties().get(SELECTED)))
                                  .findFirst().orElseThrow(() -> new IllegalStateException("Whats happening?"));
        layoutInArea(viewer,
                     btnWidth,
                     0,
                     width - btnWidth,
                     height,
                     0,
                     new Insets(0, 0, 0, 0),
                     true,
                     true,
                     HPos.CENTER,
                     VPos.CENTER);
    }

}
