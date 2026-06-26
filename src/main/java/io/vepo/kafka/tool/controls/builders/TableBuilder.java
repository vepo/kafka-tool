package io.vepo.kafka.tool.controls.builders;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.application.Platform;
import io.vepo.kafka.tool.controls.helpers.DisplayValue;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

public class TableBuilder<T> {

    public static class StringTableViewColumnBuilder<R> extends TableViewColumnBuilder<R, String> {

        StringTableViewColumnBuilder(String columnHeader, TableBuilder<R> tableBuilder) {
            super(columnHeader, tableBuilder);
        }

        public StringTableViewColumnBuilder<R> editableString(BiConsumer<R, String> onCommit) {
            tableBuilder.tableView.setEditable(true);
            column.setEditable(true);
            column.setCellFactory(col -> new TextFieldTableCell<>(new DefaultStringConverter()));
            column.setOnEditCommit(e -> {
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
                    return new SimpleStringProperty("-");
                }
                return new SimpleStringProperty(DisplayValue.ofString(getter.apply(item)));
            });
            return this;
        }

    }

    public static class TableActionsBuilder<R> {

        private static record ActionButton<T>(Node graphic, String label, String tooltip, Consumer<T> action) {}

        private class ActionButtonCell extends TableCell<R, Void> {
            private final HBox box;

            ActionButtonCell(List<ActionButton<R>> actionButtons) {
                box = new HBox(4);
                box.getStyleClass().add("table-action-buttons");
                for (var action : actionButtons) {
                    var btn = new Button();
                    if (action.graphic() != null) {
                        btn.setGraphic(action.graphic());
                        btn.getStyleClass().add("table-action-icon-button");
                    } else {
                        btn.setText(action.label());
                        btn.getStyleClass().add("table-action-text-button");
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
                setGraphic(empty ? null : box);
            }
        }

        private final List<ActionButton<R>> actions = new ArrayList<>();
        private final String columnHeader;
        private ResizePolicy resizePolicy;

        private final TableBuilder<R> tableBuilder;

        TableActionsBuilder(String columnHeader, TableBuilder<R> tableBuilder) {
            this.columnHeader = columnHeader;
            this.tableBuilder = tableBuilder;
        }

        public TableBuilder<R> add() {
            if (resizePolicy instanceof ResizePolicy.FixedSizeResizePolicy fixed) {
                tableBuilder.applyFixedSizePenalty(fixed, actions.size());
            }
            var column = new TableColumn<R, Void>(columnHeader);
            tableBuilder.addColumn(column, Optional.ofNullable(resizePolicy).orElseGet(() -> ResizePolicy.fixedSize(40)));
            column.setCellFactory(colum -> new ActionButtonCell(actions));
            return tableBuilder;
        }

        public TableActionsBuilder<R> button(String label, Consumer<R> callback) {
            actions.add(new ActionButton<>(null, label, label, callback));
            return this;
        }

        public TableActionsBuilder<R> iconButton(Node graphic, String tooltip, Consumer<R> callback) {
            actions.add(new ActionButton<>(graphic, null, tooltip, callback));
            return this;
        }

        public TableActionsBuilder<R> resizePolicy(ResizePolicy resizePolicy) {
            return width(resizePolicy);
        }

        public TableActionsBuilder<R> width(ResizePolicy resizePolicy) {
            this.resizePolicy = resizePolicy;
            return this;
        }

    }

    public static class TableViewColumnBuilder<R, C> {

        protected final TableColumn<R, C> column;
        protected ResizePolicy resizePolicy;
        protected final TableBuilder<R> tableBuilder;

        TableViewColumnBuilder(String columnHeader, TableBuilder<R> tableBuilder) {
            column = new TableColumn<>(columnHeader);
            this.tableBuilder = tableBuilder;
        }

        public TableBuilder<R> add() {
            tableBuilder.addColumn(column, Optional.ofNullable(resizePolicy).orElseGet(() -> ResizePolicy.grow(1)));
            return tableBuilder;
        }

        public TableViewColumnBuilder<R, C> editable(Callback<TableColumn<R, C>, TableCell<R, C>> callback,
                                                     BiConsumer<R, C> newValue) {
            tableBuilder.tableView.setEditable(true);
            column.setEditable(true);
            column.setCellFactory(callback);
            column.setOnEditCommit(e -> newValue.accept(e.getRowValue(), e.getNewValue()));
            return this;
        }

        public TableViewColumnBuilder<R, C> fromProperty(Function<R, C> fn) {
            column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(fn.apply(cellData.getValue())));
            return this;
        }

        public TableViewColumnBuilder<R, C> fromProperty(String property) {
            column.setCellValueFactory(new PropertyValueFactory<>(property));
            return this;
        }

        public TableViewColumnBuilder<R, C> notEditable() {
            column.setEditable(false);
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
            column.setResizable(true);
            return this;
        }

        public TableViewColumnBuilder<R, C> resizePolicy(ResizePolicy resizePolicy) {
            this.resizePolicy = resizePolicy;
            return this;
        }

    }

    private final List<ResizePolicy> resizePolicies = new ArrayList<>();

    private final List<Function<T, String>> columnTextSamples = new ArrayList<>();

    private final TableView<T> tableView;

    public TableBuilder() {
        this(new TableView<>());
    }

    TableBuilder(TableView<T> tableView) {
        this.tableView = tableView;
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    void addColumn(TableColumn<T, ?> column, ResizePolicy resizePolicy) {
        resizePolicies.add(Optional.ofNullable(resizePolicy).orElseGet(() -> ResizePolicy.grow(1)));
        columnTextSamples.add(null);
        tableView.getColumns().add(column);
    }

    void applyFixedSizePenalty(ResizePolicy.FixedSizeResizePolicy policy, int actionCount) {
        policy.setPenalty(8 * Math.max(actionCount, 1));
    }

    private void applyResizePolicies() {
        double totalWidth = tableView.widthProperty().get();
        if (totalWidth <= 0 || tableView.getColumns().isEmpty()) {
            return;
        }

        var widths = new double[resizePolicies.size()];
        double reserved = tableView.getColumns().size() + 1.0;

        for (int i = 0; i < resizePolicies.size(); i++) {
            var policy = resizePolicies.get(i);
            if (policy instanceof ResizePolicy.FixedSizeResizePolicy fixed) {
                widths[i] = fixed.size() + fixed.penalty();
                reserved += widths[i];
            } else if (policy instanceof ResizePolicy.FitContentResizePolicy fit) {
                widths[i] = measureFitContent(i, fit.minWidth(), fit.maxWidth());
                reserved += widths[i];
            }
        }

        double distributable = totalWidth - reserved;
        int growWeight = resizePolicies.stream()
                                       .filter(ResizePolicy.DistributeResizePolicy.class::isInstance)
                                       .mapToInt(policy -> ((ResizePolicy.DistributeResizePolicy) policy).weight())
                                       .sum();

        for (int i = 0; i < resizePolicies.size(); i++) {
            if (resizePolicies.get(i) instanceof ResizePolicy.DistributeResizePolicy grow) {
                widths[i] = growWeight == 0 ? 128 : Math.max(128, (grow.weight() * distributable) / growWeight);
            }
        }

        for (int i = 0; i < tableView.getColumns().size(); i++) {
            var column = tableView.getColumns().get(i);
            column.setPrefWidth(widths[i]);
            column.setMinWidth(widths[i]);
            column.setMaxWidth(widths[i]);
        }
    }

    public TableView<T> build() {
        tableView.disabledProperty().addListener((obs, oldValue, newValue) -> scheduleResizePolicies());
        tableView.widthProperty().addListener((obs, oldValue, newValue) -> scheduleResizePolicies());
        tableView.itemsProperty().addListener((obs, oldItems, newItems) -> watchItems(newItems));
        watchItems(tableView.getItems());
        return tableView;
    }

    public TableView<T> getTableView() {
        return tableView;
    }

    public TableBuilder<T> items(ObservableList<T> items) {
        tableView.setItems(items);
        return this;
    }

    public TableBuilder<T> maxWidth(double width) {
        tableView.setMaxWidth(width);
        return this;
    }

    private double measureFitContent(int columnIndex, int minWidth, int maxWidth) {
        var column = tableView.getColumns().get(columnIndex);
        double width = measureTextWidth(column.getText());
        var sampleText = columnTextSamples.get(columnIndex);
        if (sampleText != null) {
            for (var item : tableView.getItems()) {
                if (item == null) {
                    continue;
                }
                width = Math.max(width, measureTextWidth(sampleText.apply(item)));
            }
        }
        return Math.min(maxWidth, Math.max(minWidth, width));
    }

    private double measureTextWidth(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        var layout = new Text(text).getLayoutBounds();
        return layout.getWidth() + 20;
    }

    public TableBuilder<T> minHeight(double height) {
        tableView.setMinHeight(height);
        return this;
    }

    public TableBuilder<T> onSelected(Consumer<T> handler) {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> handler.accept(newValue));
        return this;
    }

    public TableBuilder<T> prefHeight(double height) {
        tableView.setPrefHeight(height);
        return this;
    }

    private void registerColumnTextSample(int columnIndex, Function<T, String> sampleText) {
        columnTextSamples.set(columnIndex, sampleText);
    }

    private void scheduleResizePolicies() {
        Platform.runLater(this::applyResizePolicies);
    }

    private void watchItems(ObservableList<T> items) {
        if (items == null) {
            return;
        }
        items.addListener((ListChangeListener<T>) change -> scheduleResizePolicies());
        scheduleResizePolicies();
    }

    public TableActionsBuilder<T> withActions(String columnHeader) {
        return new TableActionsBuilder<>(columnHeader, this);
    }

    public TableActionsBuilder<T> withButtons(String columnHeader) {
        return withActions(columnHeader);
    }

    /** Legacy column builder entry point for {@link UI#grid()} table chains. */
    public <C> TableViewColumnBuilder<T, C> withColumn(String columnHeader) {
        return new TableViewColumnBuilder<>(columnHeader, this);
    }

    public <C> TableBuilder<T> withColumn(String columnHeader, Function<T, C> getter) {
        return withColumn(columnHeader, getter, ResizePolicy.grow(1));
    }

    public <C> TableBuilder<T> withColumn(String columnHeader, Function<T, C> getter, ResizePolicy resizePolicy) {
        var column = new TableColumn<T, String>(columnHeader);
        column.setCellValueFactory(cellData -> new SimpleStringProperty(DisplayValue.ofNullable(getter.apply(cellData.getValue()))));
        column.setEditable(false);
        addColumn(column, resizePolicy);
        registerColumnTextSample(tableView.getColumns().size() - 1, row -> {
            if (row == null) {
                return "";
            }
            return DisplayValue.ofNullable(getter.apply(row));
        });
        return this;
    }

    public TableBuilder<T> withPropertyColumn(String columnHeader, String property, ResizePolicy resizePolicy) {
        new TableViewColumnBuilder<>(columnHeader, this).fromProperty(property).notEditable().resizePolicy(resizePolicy).add();
        return this;
    }

    /**
     * Legacy string column builder entry point for {@link UI#grid()} table chains.
     */
    public StringTableViewColumnBuilder<T> withStringColumn(String columnHeader) {
        return new StringTableViewColumnBuilder<>(columnHeader, this);
    }

    public TableBuilder<T> withStringColumn(String columnHeader, Function<T, String> getter) {
        return withStringColumn(columnHeader, getter, ResizePolicy.grow(1));
    }

    public TableBuilder<T> withStringColumn(String columnHeader, Function<T, String> getter, ResizePolicy resizePolicy) {
        new StringTableViewColumnBuilder<>(columnHeader, this).fromString(getter).notEditable().resizePolicy(resizePolicy).add();
        registerColumnTextSample(tableView.getColumns().size() - 1, row -> {
            if (row == null) {
                return "";
            }
            return DisplayValue.ofString(getter.apply(row));
        });
        return this;
    }

}
