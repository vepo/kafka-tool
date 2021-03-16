package io.vepo.kt;

import static io.vepo.kt.UiConstants.PADDING;

import io.vepo.kt.KafkaAdminService.BrokerStatus;
import io.vepo.kt.KafkaAdminService.KafkaConnectionWatcher;
import io.vepo.kt.KafkaAdminService.TopicInfo;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TopicsView extends TableView<TopicInfo> implements KafkaConnectionWatcher {

    private KafkaAdminService adminService;

    public TopicsView(KafkaAdminService adminService) {
        this.adminService = adminService;
        this.adminService.watch(this);

        setEditable(false);
        var nameColumn = new TableColumn<TopicInfo, String>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<TopicInfo, String>("name"));
        nameColumn.setResizable(false);
        getColumns().add(nameColumn);

        var internalColumn = new TableColumn<TopicInfo, Boolean>("Internal?");
        internalColumn.setCellValueFactory(new PropertyValueFactory<TopicInfo, Boolean>("internal"));
        internalColumn.setResizable(false);
        internalColumn.setReorderable(false);
        getColumns().add(internalColumn);

        var actionsColumn = new TableColumn<TopicInfo, Void>("Actions");
        actionsColumn.setResizable(false);
        actionsColumn.setReorderable(false);
        actionsColumn.setCellFactory((column) -> new ActionButtonCell());
        actionsColumn.setMinWidth((2 * PADDING) + 128);
        actionsColumn.setPrefWidth((2 * PADDING) + 128);
        getColumns().add(actionsColumn);

        DoubleProperty width = new SimpleDoubleProperty();
        width.bind(internalColumn.widthProperty().add(actionsColumn.widthProperty()));
        width.addListener((ov, t, t1) -> {
            nameColumn.setPrefWidth(getWidth() - (2 * PADDING) - t1.doubleValue());
        });

        nameColumn.setPrefWidth(getWidth() - (2 * PADDING) - width.doubleValue());
        nameColumn.setResizable(false);

        widthProperty().addListener((ov, t, t1) -> {
            nameColumn.setPrefWidth(getWidth() - (2 * PADDING) - width.doubleValue());
        });
    }

    public class ActionButtonCell extends TableCell<TopicInfo, Void> {
        private HBox box;

        public ActionButtonCell() {
            box = new HBox(PADDING);

            var btnAlter = new Button("Alter");
            btnAlter.setDisable(true);
            btnAlter.setMinWidth(64);
            box.getChildren().add(btnAlter);

            var btnSubscribe = new Button("Subscribe");
            btnSubscribe.setMinWidth(64);
            btnSubscribe.setOnAction(e -> {
                var consumerStage = new TopicSubscribeStage(getTableRow().itemProperty()
                                                                         .get()
                                                                         .getName(),
                                                            (Stage) getScene().getWindow(),
                                                            Settings.getInstance()
                                                                    .clone());
                consumerStage.show();
            });
            box.getChildren().add(btnSubscribe);
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

    @Override
    public void statusChanged(BrokerStatus status) {
        switch (status) {
            case IDLE:
                Platform.runLater(this.getItems()::clear);
            case CONNECTED:
                this.adminService.listTopics(topics -> Platform.runLater(() -> this.getItems().addAll(topics)));
            default:
                break;
        }
    }

}
