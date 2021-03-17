package io.vepo.kt;

public class TopicsView {// extends TableView<TopicInfo> implements KafkaConnectionWatcher {

//    private KafkaAdminService adminService;
//
//    public TopicsView(KafkaAdminService adminService) {
//        this.adminService = adminService;
//        this.adminService.watch(this);
//
//        setEditable(false);
//        var nameColumn = new TableColumn<TopicInfo, String>("Name");
//        nameColumn.setCellValueFactory(new PropertyValueFactory<TopicInfo, String>("name"));
//        nameColumn.setResizable(false);
//        getColumns().add(nameColumn);
//
//        var internalColumn = new TableColumn<TopicInfo, Boolean>("Internal?");
//        internalColumn.setCellValueFactory(new PropertyValueFactory<TopicInfo, Boolean>("internal"));
//        internalColumn.setResizable(false);
//        internalColumn.setReorderable(false);
//        getColumns().add(internalColumn);
//
//        var actionsColumn = new TableColumn<TopicInfo, Void>("Actions");
//        actionsColumn.setResizable(false);
//        actionsColumn.setReorderable(false);
//        actionsColumn.setCellFactory((column) -> new ActionButtonCell());
//        actionsColumn.setMinWidth((3 * PADDING) + 192);
//        actionsColumn.setPrefWidth((3 * PADDING) + 192);
//        getColumns().add(actionsColumn);
//
//        DoubleProperty width = new SimpleDoubleProperty();
//        width.bind(internalColumn.widthProperty().add(actionsColumn.widthProperty()));
//        width.addListener((ov, t, t1) -> {
//            nameColumn.setPrefWidth(getWidth() - (2 * PADDING) - t1.doubleValue());
//        });
//
//        nameColumn.setResizable(false);
//
//        widthProperty().addListener((ov, t, t1) -> {
//            nameColumn.setPrefWidth(getWidth() - (2 * PADDING) - width.doubleValue());
//        });
//    }
//
//    public class ActionButtonCell extends TableCell<TopicInfo, Void> {
//        private HBox box;
//
//        public ActionButtonCell() {
//            box = new HBox(PADDING);
//
//            var btnAlter = new Button("Alter");
//            btnAlter.setDisable(true);
//            btnAlter.setMinWidth(64);
//            box.getChildren().add(btnAlter);
//
//            var btnEmpty = new Button("Empty");
//            btnEmpty.setMinWidth(64);
//            btnEmpty.setOnAction(e -> {
//                var alert = new Alert(AlertType.CONFIRMATION, "All messages will be lost", ButtonType.OK,
//                                      ButtonType.CANCEL);
//                alert.setTitle("Do you really want to clear the topic?");
//                alert.show();
//                alert.resultProperty().addListener((obs, oldValue, newValue) -> {
//                    if (newValue == ButtonType.OK) {
//                        adminService.emptyTopic(getTableRow().itemProperty()
//                                                             .get());
//                    }
//                });
//
//            });
//            box.getChildren().add(btnEmpty);
//
//            var btnSubscribe = new Button("Subscribe");
//            btnSubscribe.setMinWidth(64);
//            btnSubscribe.setOnAction(e -> {
//                var consumerStage = new TopicSubscribeStage(getTableRow().itemProperty()
//                                                                         .get()
//                                                                         .getName(),
//                                                            (Stage) getScene().getWindow(),
//                                                            Settings.kafka()
//                                                                    .clone()
//                                                                    .getBrokers()
//                                                                    .get(0)
//                                                                    .clone());
//                consumerStage.show();
//            });
//            box.getChildren().add(btnSubscribe);
//        }
//
//        @Override
//        public void updateItem(Void item, boolean empty) {
//            super.updateItem(item, empty);
//            if (empty) {
//                setGraphic(null);
//            } else {
//                setGraphic(box);
//            }
//        }
//    }
//
//    @Override
//    public void statusChanged(BrokerStatus status) {
//        switch (status) {
//            case IDLE:
//                clear();
//            case CONNECTED:
//                update();
//            default:
//                break;
//        }
//    }
//
//    public void clear() {
//        Platform.runLater(this.getItems()::clear);
//    }
//
//    public void update() {
//        this.adminService.listTopics(topics -> runLater(() -> {
//            this.getItems().clear();
//            this.getItems().addAll(topics);
//        }));
//    }

}
