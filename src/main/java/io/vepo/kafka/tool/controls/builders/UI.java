package io.vepo.kafka.tool.controls.builders;

import io.vepo.kafka.tool.controls.EmptyStatePane;
import io.vepo.kafka.tool.controls.MainWindowPane;
import io.vepo.kafka.tool.controls.ProgressStatusBar;
import io.vepo.kafka.tool.controls.ViewActionBar;
import io.vepo.kafka.tool.controls.ViewHeader;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Fluent entry point for building Kafka Tool UI. Views and stages compose
 * screens through this API instead of constructing JavaFX nodes directly.
 */
public final class UI {

    public static final class ActionBarBuilder {

        private Button refreshButton;
        private CheckBox autoRefresh;
        private Button disconnectButton;

        public ActionBarBuilder autoRefresh(CheckBox checkBox) {
            this.autoRefresh = checkBox;
            return this;
        }

        public ActionBarBuilder autoRefresh(String label, Consumer<Boolean> onToggle,
                                            javafx.beans.property.BooleanProperty property) {
            var checkBox = new CheckBox(label);
            checkBox.selectedProperty().bindBidirectional(property);
            checkBox.setOnAction(e -> onToggle.accept(checkBox.isSelected()));
            return autoRefresh(checkBox);
        }

        public ViewActionBar build() {
            if (refreshButton == null || disconnectButton == null) {
                throw new IllegalStateException("Action bar requires refresh and disconnect buttons.");
            }
            if (autoRefresh != null) {
                return new ViewActionBar(refreshButton, autoRefresh, disconnectButton);
            }
            return new ViewActionBar(refreshButton, disconnectButton);
        }

        public ActionBarBuilder button(String label, Runnable action) {
            throw new UnsupportedOperationException("Use refresh(), autoRefresh(), and disconnect() on UI.actionBar().");
        }

        public ActionBarBuilder disconnect(String label, Runnable action) {
            var button = new Button(label);
            button.setOnAction(e -> action.run());
            disconnectButton = button;
            return this;
        }

        public ActionBarBuilder refresh(String label, Runnable action) {
            var button = new Button(label);
            button.setOnAction(e -> action.run());
            refreshButton = button;
            return this;
        }

    }

    public static final class MainViewBuilder {

        private String title;
        private String description;
        private boolean mainWindowHeader;
        private ViewMessageModel messageModel;
        private final List<Node> bodyNodes = new ArrayList<>();
        private Node actionBar;
        private Insets padding = Insets.EMPTY;
        private double spacing = 0;

        public MainViewBuilder actionBar(Node bar) {
            actionBar = bar;
            return this;
        }

        public MainViewBuilder body(Node node) {
            return body(node, Priority.ALWAYS);
        }

        public MainViewBuilder body(Node node, Priority verticalGrow) {
            if (node instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
            }
            VBox.setVgrow(node, verticalGrow);
            bodyNodes.add(node);
            return this;
        }

        public VBox build() {
            var pane = new VBox(spacing);
            pane.setFillWidth(true);
            pane.setPadding(padding);

            var header = new ViewHeader(title, description);
            if (messageModel != null) {
                header.bindMessage(messageModel);
            }
            if (mainWindowHeader) {
                header.getStyleClass().add("main-window-view-header");
            }
            VBox.setVgrow(header, Priority.NEVER);
            pane.getChildren().add(header);

            for (var node : bodyNodes) {
                pane.getChildren().add(node);
            }

            if (actionBar != null) {
                if (actionBar instanceof Region region) {
                    region.setMaxHeight(region.prefHeight(-1));
                }
                VBox.setVgrow(actionBar, Priority.NEVER);
                pane.getChildren().add(actionBar);
            }

            return pane;
        }

        public MainViewBuilder mainWindowHeader() {
            mainWindowHeader = true;
            return this;
        }

        public MainViewBuilder message(ViewMessageModel messageModel) {
            this.messageModel = messageModel;
            return this;
        }

        public MainViewBuilder padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        public MainViewBuilder spacing(double spacing) {
            this.spacing = spacing;
            return this;
        }

        public MainViewBuilder title(String title, String description) {
            this.title = title;
            this.description = description;
            return this;
        }

    }

    public static final class MainWindowBuilder {

        private record TabEntry(String label, Node content) {}

        private final List<TabEntry> tabs = new ArrayList<>();

        public MainWindowPane build() {
            var pane = new MainWindowPane();
            pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            for (var tab : tabs) {
                if (tab.content() instanceof Region region) {
                    region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                }
                pane.add(tab.label(), tab.content());
            }
            return pane;
        }

        public MainWindowBuilder tab(String label, Node content) {
            tabs.add(new TabEntry(label, content));
            return this;
        }

    }

    public static ActionBarBuilder actionBar() {
        return new ActionBarBuilder();
    }

    public static ObservableValue<String> bindInt(javafx.beans.property.ObjectProperty<?> source,
                                                  ToIntFunction<Object> extractor) {
        return Bindings.createStringBinding(() -> {
            var value = source.get();
            return value == null ? "—" : String.valueOf(extractor.applyAsInt(value));
        }, source);
    }

    public static ObservableValue<String> bindString(javafx.beans.property.ObjectProperty<?> source,
                                                     Function<Object, String> extractor) {
        return Bindings.createStringBinding(() -> {
            var value = source.get();
            return value == null ? "—" : extractor.apply(value);
        }, source);
    }

    public static ScreenBuilder.GridScreenBuilder grid() {
        return ScreenBuilder.grid();
    }

    public static MainViewBuilder mainView() {
        return new MainViewBuilder();
    }

    public static MainWindowBuilder mainWindow() {
        return new MainWindowBuilder();
    }

    public static ProgressStatusBar progressBar(int rowSpan) {
        return new ProgressStatusBar(rowSpan);
    }

    public static ScrollPane scroll(Node content) {
        var scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxWidth(Double.MAX_VALUE);
        return scroll;
    }

    public static Label section(String text) {
        var label = new Label(text);
        label.getStyleClass().add("cluster-section-label");
        return label;
    }

    public static VBox statCard(String title, ObservableValue<String> valueBinding) {
        var titleLabel = new Label(title);
        titleLabel.getStyleClass().add("cluster-stat-label");
        var valueLabel = new Label("—");
        valueLabel.getStyleClass().add("cluster-stat-value");
        valueLabel.textProperty().bind(valueBinding);
        var card = new VBox(4, titleLabel, valueLabel);
        card.getStyleClass().add("cluster-stat-card");
        card.setMinWidth(88);
        return card;
    }

    public static HBox summaryBar(Node... cards) {
        var bar = new HBox(10, cards);
        bar.getStyleClass().add("cluster-summary-bar");
        return bar;
    }

    public static <T> TableBuilder<T> table() {
        return new TableBuilder<>();
    }

    public static StackPane tableWithEmptyState(TableView<?> table, ObservableList<?> items, String emptyMessage) {
        var emptyState = new EmptyStatePane(emptyMessage);
        var stack = new StackPane(table, emptyState);
        stack.setMaxWidth(Double.MAX_VALUE);
        emptyState.visibleProperty().bind(Bindings.isEmpty(items));
        emptyState.managedProperty().bind(emptyState.visibleProperty());
        return stack;
    }

    public static GridPane twoColumns(Node left, Node right) {
        return twoColumns(left, right, 320);
    }

    public static GridPane twoColumns(Node left, Node right, double leftMaxWidth) {
        var grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);

        var leftColumn = new ColumnConstraints();
        leftColumn.setHgrow(Priority.NEVER);
        leftColumn.setMaxWidth(leftMaxWidth);
        leftColumn.setPrefWidth(leftMaxWidth);
        leftColumn.setFillWidth(true);

        var rightColumn = new ColumnConstraints();
        rightColumn.setHgrow(Priority.ALWAYS);
        rightColumn.setFillWidth(true);

        grid.getColumnConstraints().addAll(leftColumn, rightColumn);
        grid.add(left, 0, 0);
        grid.add(right, 1, 0);
        GridPane.setHgrow(left, Priority.NEVER);
        if (left instanceof Region leftRegion) {
            leftRegion.setMaxWidth(leftMaxWidth);
        }
        GridPane.setHgrow(right, Priority.ALWAYS);
        GridPane.setVgrow(left, Priority.ALWAYS);
        GridPane.setVgrow(right, Priority.ALWAYS);
        GridPane.setFillWidth(left, true);
        GridPane.setFillWidth(right, true);
        return grid;
    }

    public static VBox verticalSection(double spacing, Node... nodes) {
        var box = new VBox(spacing, nodes);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private UI() {}

}
