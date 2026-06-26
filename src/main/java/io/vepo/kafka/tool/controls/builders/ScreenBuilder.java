package io.vepo.kafka.tool.controls.builders;

import static javafx.collections.FXCollections.observableArrayList;

import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.controls.helpers.WindowHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public interface ScreenBuilder {

    class GridScreenBuilder implements ScreenBuilder {

        private int currentColumn;
        private int currentRow;
        private GridPane pane;
        private ViewHeader viewHeader;

        private GridScreenBuilder() {
            pane = new GridPane();
            pane.getStyleClass().add("screen-grid");
            pane.setAlignment(Pos.TOP_LEFT);
            pane.setHgap(10);
            pane.setVgap(10);
            pane.setPadding(new Insets(25, 25, 25, 25));

            var labelColumn = new ColumnConstraints();
            labelColumn.setMinWidth(140);
            labelColumn.setPrefWidth(140);

            var fieldColumn = new ColumnConstraints();
            fieldColumn.setHgrow(Priority.ALWAYS);
            fieldColumn.setFillWidth(true);

            pane.getColumnConstraints().addAll(labelColumn, fieldColumn);
            currentRow = 0;
            currentColumn = 0;
        }

        public Button addButton(String label) {
            return addButton(label, 1);
        }

        public Button addButton(String label, int colSpan) {
            var btn = new Button(label);
            int columnIndex = currentColumn++;
            int rowIndex = currentRow;
            GridPane.setFillHeight(btn, true);
            GridPane.setColumnSpan(btn, colSpan);
            btn.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(btn, Priority.SOMETIMES);
            pane.add(btn, columnIndex, rowIndex);
            return btn;
        }

        public <T> ComboBox<T> addComboBox() {
            return addComboBox(observableArrayList());
        }

        public <T> ComboBox<T> addComboBox(javafx.collections.ObservableList<T> items) {
            return addComboBox(items, 1);
        }

        public <T> ComboBox<T> addComboBox(javafx.collections.ObservableList<T> items, int colSpan) {
            var combo = new ComboBox<>(items);
            pane.add(combo, currentColumn++, currentRow);
            combo.setMaxWidth(Double.MAX_VALUE);
            GridPane.setColumnSpan(combo, colSpan);
            GridPane.setHgrow(combo, Priority.ALWAYS);
            return combo;
        }

        public <T extends Node> T addCustom(T control) {
            return addCustom(control, 1);
        }

        public <T extends Node> T addCustom(T control, int colSpan) {
            pane.add(control, currentColumn++, currentRow);
            GridPane.setHgrow(control, Priority.ALWAYS);
            GridPane.setColumnSpan(control, colSpan);
            return control;
        }

        public <T> TableBuilder<T> addTableView(int colSpan) {
            var table = new TableView<T>();
            pane.add(table, currentColumn++, currentRow);
            GridPane.setHgrow(table, Priority.ALWAYS);
            GridPane.setColumnSpan(table, colSpan);
            GridPane.setVgrow(table, Priority.ALWAYS);
            return new TableBuilder<>(table);
        }

        public Label addText(String label) {
            var txt = new Label(label);
            txt.getStyleClass().add("form-label");
            txt.setMaxWidth(Double.MAX_VALUE);
            pane.add(txt, currentColumn++, currentRow);
            return txt;
        }

        public TextArea addTextArea() {
            var txt = new TextArea();
            pane.add(txt, currentColumn++, currentRow);
            GridPane.setHgrow(txt, Priority.ALWAYS);
            GridPane.setVgrow(txt, Priority.ALWAYS);
            return txt;
        }

        public TextField addTextField() {
            var txt = new TextField();
            txt.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(txt, true);
            pane.add(txt, currentColumn++, currentRow);
            GridPane.setHgrow(txt, Priority.ALWAYS);
            return txt;
        }

        public Label addValidationLabel(int colSpan) {
            var label = new Label();
            label.getStyleClass().add("validation-message");
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setManaged(false);
            label.setVisible(false);
            pane.add(label, currentColumn, currentRow);
            GridPane.setColumnSpan(label, colSpan);
            currentColumn += colSpan;
            return label;
        }

        @Override
        public Scene build() {
            var root = WindowHelper.rootControl();
            root.setMain(wrapContent());
            return new Scene(root);
        }

        @Override
        public Scene build(int width, int height) {
            return new Scene(wrapContent(), width, height);
        }

        public GridPane getGridPane() {
            return pane;
        }

        public ViewHeader getViewHeader() {
            return viewHeader;
        }

        public GridScreenBuilder newLine() {
            currentColumn = 0;
            currentRow++;
            return this;
        }

        public GridScreenBuilder skipCell() {
            currentColumn++;
            return this;
        }

        public GridScreenBuilder withViewHeader(String title, String description) {
            viewHeader = new ViewHeader(title, description);
            return this;
        }

        private Parent wrapContent() {
            if (viewHeader == null) {
                return pane;
            }
            var wrapper = new VBox();
            wrapper.getChildren().addAll(viewHeader, pane);
            VBox.setVgrow(pane, Priority.ALWAYS);
            return wrapper;
        }

    }

    static GridScreenBuilder grid() {
        return new GridScreenBuilder();
    }

    Scene build();

    Scene build(int width, int height);

}
