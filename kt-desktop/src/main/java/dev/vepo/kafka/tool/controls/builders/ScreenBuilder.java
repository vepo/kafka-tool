package dev.vepo.kafka.tool.controls.builders;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import dev.vepo.kafka.tool.controls.builders.ResizePolicy.FixedSizeResizePolicy;
import dev.vepo.kafka.tool.controls.helpers.WindowHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.Callback;

public interface ScreenBuilder {
    public class GridScreenBuilder implements ScreenBuilder {
        private int currentColumn;
        private int currentRow;
        private GridPane pane;

        private GridScreenBuilder() {
            pane = new GridPane();
            pane.setAlignment(Pos.CENTER);
            pane.setHgap(10);
            pane.setVgap(10);
            pane.setPadding(new Insets(25, 25, 25, 25));
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

        public <T> ComboBox<T> addComboBox(ObservableList<T> items) {
            return addComboBox(items, 1);
        }

        public <T> ComboBox<T> addComboBox(ObservableList<T> items, int colSpan) {
            var combo = new ComboBox<T>(items);
            pane.add(combo, currentColumn++, currentRow);
            combo.setMaxWidth(Double.MAX_VALUE);
            GridPane.setColumnSpan(combo, colSpan);
            GridPane.setHgrow(combo, Priority.ALWAYS);
            return combo;

        }

        public <T> TableViewBuilder<T> addTableView(int colSpan) {
            var table = new TableView<T>();
            pane.add(table, currentColumn++, currentRow);
            GridPane.setHgrow(table, Priority.ALWAYS);
            GridPane.setColumnSpan(table, colSpan);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            GridPane.setVgrow(table, Priority.ALWAYS);
            return new TableViewBuilder<T>(table);
        }

        public Text addText(String label) {
            var txt = new Text(label);
            pane.add(txt, currentColumn++, currentRow);
            return txt;
        }

        public TextField addTextField() {
            var txt = new TextField();
            pane.add(txt, currentColumn++, currentRow);
            GridPane.setHgrow(txt, Priority.ALWAYS);
            return txt;
        }

        public Spinner<Integer> addIntegerField(int min, int max, int initialValue) {
            return addIntegerField(min, max, initialValue, 1);
        }

        public Spinner<Integer> addIntegerField(int min, int max, int initialValue, int amountToStepBy) {
            Spinner<Integer> spinner = new Spinner<>();
            pane.add(spinner, currentColumn++, currentRow);
            spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue,
                                                                                       amountToStepBy));
            GridPane.setHgrow(spinner, Priority.ALWAYS);
            return spinner;
        }

        public TextArea addTextArea() {
            var txt = new TextArea();
            pane.add(txt, currentColumn++, currentRow);
            GridPane.setHgrow(txt, Priority.ALWAYS);
            GridPane.setVgrow(txt, Priority.ALWAYS);
            return txt;
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

        @Override
        public Scene build() {
            var root = WindowHelper.rootControl();
            root.setMain(pane);
            return new Scene(root);
        }

        @Override
        public Scene build(int width, int height) {
            return new Scene(pane, width, height);
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

    }

    public class TableViewBuilder<T> {

        private List<ResizePolicy> resizePolicies;
        private TableView<T> tableView;

        private TableViewBuilder(TableView<T> tableView) {
            this.tableView = tableView;
            this.resizePolicies = new ArrayList<>();
        }

        public TableView<T> build() {
            tableView.disabledProperty().addListener((obs, oldValue, newValue) -> ResizePolicy.apply(resizePolicies,
                                                                                                     tableView.widthProperty()
                                                                                                              .get(),
                                                                                                     (index, width) -> {
                                                                                                         tableView.getColumns()
                                                                                                                  .get(index)
                                                                                                                  .setPrefWidth(width);
                                                                                                         tableView.getColumns()
                                                                                                                  .get(index)
                                                                                                                  .setMinWidth(width);
                                                                                                         tableView.getColumns()
                                                                                                                  .get(index)
                                                                                                                  .setMaxWidth(width);
                                                                                                     }));
            tableView.widthProperty().addListener((obs, oldValue, newValue) -> ResizePolicy.apply(resizePolicies,
                                                                                                  newValue.doubleValue(),
                                                                                                  (index, width) -> {
                                                                                                      tableView.getColumns()
                                                                                                               .get(index)
                                                                                                               .setPrefWidth(width);
                                                                                                      tableView.getColumns()
                                                                                                               .get(index)
                                                                                                               .setMinWidth(width);
                                                                                                      tableView.getColumns()
                                                                                                               .get(index)
                                                                                                               .setMaxWidth(width);
                                                                                                  }));
            return tableView;
        }

        public TableViewButtonsColumnBuilder<T> withButtons(String columnHeader) {
            return new TableViewButtonsColumnBuilder<T>(columnHeader, this);
        }

        public <C> TableViewColumnBuilder<T, C> withColumn(String columnHeader) {
            return new TableViewColumnBuilder<T, C>(columnHeader, this);
        }

    }

    public class TableViewButtonsColumnBuilder<R> {

        private class ActionButtonCell<T> extends TableCell<T, Void> {
            private HBox box;

            public ActionButtonCell(Map<String, Consumer<T>> buttons) {
                box = new HBox(10);
                buttons.forEach((label, action) -> {
                    var btn = new Button(label);
                    btn.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(btn, Priority.ALWAYS);
                    btn.setOnAction(e -> action.accept(getTableRow().itemProperty().get()));
                    box.getChildren().add(btn);

                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        }

        private final Map<String, Consumer<R>> buttons;
        private TableColumn<R, Void> column;
        private ResizePolicy resizePolicy;

        private TableViewBuilder<R> tableBuilder;

        private TableViewButtonsColumnBuilder(String columnHeader, TableViewBuilder<R> tableBuilder) {
            column = new TableColumn<R, Void>(columnHeader);
            buttons = new HashMap<>();
            column.setCellFactory(colum -> new ActionButtonCell<R>(buttons));
            this.tableBuilder = tableBuilder;
        }

        public TableViewBuilder<R> add() {
            if (resizePolicy instanceof FixedSizeResizePolicy) {
                ((FixedSizeResizePolicy) resizePolicy).setPenalty(10 * Math.max(buttons.size(), 2));
            }
            tableBuilder.resizePolicies.add(Optional.ofNullable(resizePolicy).orElseGet(() -> ResizePolicy.grow(1)));
            tableBuilder.tableView.getColumns().add(column);
            return tableBuilder;
        }

        public TableViewButtonsColumnBuilder<R> button(String label, Consumer<R> callback) {
            this.buttons.put(label, callback);
            return this;
        }

        public TableViewButtonsColumnBuilder<R> resizePolicy(ResizePolicy resizePolicy) {
            this.resizePolicy = resizePolicy;
            return this;
        }

    }

    public class TableViewColumnBuilder<R, C> {

        private TableColumn<R, C> column;
        private ResizePolicy resizePolicy;
        private TableViewBuilder<R> tableBuilder;

        private TableViewColumnBuilder(String columnHeader, TableViewBuilder<R> tableBuilder) {
            column = new TableColumn<R, C>(columnHeader);
            this.tableBuilder = tableBuilder;
        }

        public TableViewBuilder<R> add() {
            tableBuilder.resizePolicies.add(Optional.ofNullable(resizePolicy).orElseGet(() -> ResizePolicy.grow(1)));
            tableBuilder.tableView.getColumns().add(column);
            return tableBuilder;
        }

        public TableViewColumnBuilder<R, C> editable(Callback<TableColumn<R, C>, TableCell<R, C>> callback,
                                                     BiConsumer<R, C> newValue) {
            this.tableBuilder.tableView.setEditable(true);
            this.column.setEditable(true);
            this.column.setCellFactory(callback);
            this.column.setOnEditCommit(e -> newValue.accept(e.getRowValue(), e.getNewValue()));
            return this;
        }

        public TableViewColumnBuilder<R, C> fromProperty(String property) {
            column.setCellValueFactory(new PropertyValueFactory<R, C>(property));
            return this;
        }

        public TableViewColumnBuilder<R, C> fromProperty(Function<R, C> fn) {
            column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(fn.apply(cellData.getValue())));
            return this;
        }

        public TableViewColumnBuilder<R, C> notEditable() {
            this.column.setEditable(false);
            return this;
        }

        public TableViewColumnBuilder<R, C> notReorderable() {
            column.setReorderable(false);
            return this;
        }

        public TableViewColumnBuilder<R, C> notResizable() {
            column.setResizable(false);
            return this;
        }

        public TableViewColumnBuilder<R, C> reorderable() {
            column.setReorderable(true);
            return this;
        }

        public TableViewColumnBuilder<R, C> resizable() {
            column.setReorderable(true);
            return this;
        }

        public TableViewColumnBuilder<R, C> resizePolicy(ResizePolicy resizePolicy) {
            this.resizePolicy = resizePolicy;
            return this;
        }

    }

    public static GridScreenBuilder grid() {
        return new GridScreenBuilder();
    }

    public Scene build();

    public Scene build(int width, int height);
}
