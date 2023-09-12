package dev.vepo.kafka.tool.controls.helpers;

import dev.vepo.kafka.tool.controls.WindowHead;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class WindowHelper {
    public static class RootControl extends BorderPane {
        public RootControl() {
            super();
        }

        public void setMain(Node main) {
            setCenter(main);
        }
    }

    public static RootControl rootControl() {
        var main = new RootControl();
        var head = new WindowHead();
        main.setTop(head);
        return main;
    }

}
