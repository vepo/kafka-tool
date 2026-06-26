package io.vepo.kafka.tool.controls.builders;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.controls.builders.ResizePolicy.FixedSizeResizePolicy;
import io.vepo.kafka.tool.controls.helpers.WindowHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.DefaultStringConverter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.util.Callback;

public interface ScreenBuilder {
    public class GridScreenBuilder implements ScreenBuilder {
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

        public <T extends Node> T addCustom(T control) {
            return addCustom(control, 1);
        }

        public <T extends Node> T addCustom(T control, int colSpan) {
            pane.add(control, currentColumn++, currentRow);
            GridPane.setHgrow(control, Priority.ALWAYS);
            GridPane.setColumnSpan(control, colSpan);
            return control;
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

    public class StringTableViewColumnBuilder<R> extends TableViewColumnBuilder<R, String> {

        private StringTableViewColumnBuilder(String columnHeader, TableViewBuilder<R> tableBuilder) {
            super(columnHeader, tableBuilder);
        }

        public StringTableViewColumnBuilder<R> editableString(BiConsumer<R, String> onCommit) {
            this.tableBuilder.tableView.setEditable(true);
            this.column.setEditable(true);
            this.column.setCellFactory(col -> new TextFieldTableCell<R, String>(new DefaultStringConverter()));
            this.column.setOnEditCommit(e -> {
                if (e.getRowValue() != null && e.getNewValue() != null) {
                    onCommit.accept(e.getRowValue(), e.getNewValue());
                }
            });
            return this;
        }

        public StringTableViewColumnBuilder<R> fromString(Function<R, String> getter) {
            column.setCellValueFactory(cellData -> {
                var item = cellData.getValue();
                if (item == null) {
                    return new SimpleStringProperty("");
                }
                var value = getter.apply(item);
                return new SimpleStringProperty(value == null ? "" : value);
            });
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
                                                                                                     tableView.widthProperty().get(), (index, width) -> {
                                                                                                         tableView.getColumns().get(index).setPrefWidth(width);
                                                                                                         tableView.getColumns().get(index).setMinWidth(width);
                                                                                                         tableView.getColumns().get(index).setMaxWidth(width);
                                                                                                     }));
            tableView.widthProperty().addListener((obs, oldValue, newValue) -> ResizePolicy.apply(resizePolicies,
                                                                                                  newValue.doubleValue(), (index, width) -> {
                                                                                                      tableView.getColumns().get(index).setPrefWidth(width);
                                                                                                      tableView.getColumns().get(index).setMinWidth(width);
                                                                                                      tableView.getColumns().get(index).setMaxWidth(width);
                                                                                                  }));
            return tableView;
        }

        public TableViewButtonsColumnBuilder<T> withButtons(String columnHeader) {
            return new TableViewButtonsColumnBuilder<T>(columnHeader, this);
        }

        public <C> TableViewColumnBuilder<T, C> withColumn(String columnHeader) {
            return new TableViewColumnBuilder<T, C>(columnHeader, this);
        }

        public StringTableViewColumnBuilder<T> withStringColumn(String columnHeader) {
            return new StringTableViewColumnBuilder<T>(columnHeader, this);
        }

    }

    public class TableViewButtonsColumnBuilder<R> {

        private static record ActionButton<T>(Node graphic, String label, String tooltip, Consumer<T> action) {}

        private class ActionButtonCell extends TableCell<R, Void> {
            private final HBox box;

            public ActionButtonCell(List<ActionButton<R>> actions) {
                box = new HBox(4);
                box.getStyleClass().add("table-action-buttons");
                for (var action : actions) {
                    var btn = new Button();
                    if (action.graphic() != null) {
                        btn.setGraphic(action.graphic());
                        btn.getStyleClass().add("table-action-icon-button");
                    } else {
                        btn.setText(action.label());
                    }
                    if (action.tooltip() != null && !action.tooltip().isBlank()) {
                        btn.setTooltip(new Tooltip(action.tooltip()));
                    }
                    btn.setOnAction(e -> action.action().accept(getTableRow().itemProperty().get()));
                    box.getChildren().add(btn);
                }
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

        private final List<ActionButton<R>> actions = new ArrayList<>();
        private TableColumn<R, Void> column;
        private ResizePolicy resizePolicy;

        private TableViewBuilder<R> tableBuilder;

        private TableViewButtonsColumnBuilder(String columnHeader, TableViewBuilder<R> tableBuilder) {
            column = new TableColumn<R, Void>(columnHeader);
            this.tableBuilder = tableBuilder;
        }

        public TableViewBuilder<R> add() {
            if (resizePolicy instanceof FixedSizeResizePolicy) {
                ((FixedSizeResizePolicy) resizePolicy).setPenalty(8 * Math.max(actions.size(), 1));
            }
            tableBuilder.resizePolicies.add(Optional.ofNullable(resizePolicy).orElseGet(() -> ResizePolicy.fixedSize(40)));
            tableBuilder.tableView.getColumns().add(column);
            column.setCellFactory(colum -> new ActionButtonCell(actions));
            return tableBuilder;
        }

        public TableViewButtonsColumnBuilder<R> button(String label, Consumer<R> callback) {
            actions.add(new ActionButton(null, label, label, callback));
            return this;
        }

        public TableViewButtonsColumnBuilder<R> iconButton(Node graphic, String tooltip, Consumer<R> callback) {
            actions.add(new ActionButton(graphic, null, tooltip, callback));
            return this;
        }

        public TableViewButtonsColumnBuilder<R> resizePolicy(ResizePolicy resizePolicy) {
            this.resizePolicy = resizePolicy;
            return this;
        }

    }

    public class TableViewColumnBuilder<R, C> {

        protected TableColumn<R, C> column;
        protected ResizePolicy resizePolicy;
        protected TableViewBuilder<R> tableBuilder;

        protected TableViewColumnBuilder(String columnHeader, TableViewBuilder<R> tableBuilder) {
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

        public TableViewColumnBuilder<R, C> fromProperty(Function<R, C> fn) {
            column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(fn.apply(cellData.getValue())));
            return this;
        }

        public TableViewColumnBuilder<R, C> fromProperty(String property) {
            column.setCellValueFactory(new PropertyValueFactory<R, C>(property));
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
