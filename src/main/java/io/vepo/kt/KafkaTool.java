package io.vepo.kt;

import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import io.vepo.kt.KafkaAdminService.BrokerStatus;
import io.vepo.kt.KafkaAdminService.KafkaConnectionWatcher;
import io.vepo.kt.ScreenBuilder.GridBagFormBuilder;
import io.vepo.kt.settings.Settings;
import io.vepo.kt.settings.UiSettings;
import io.vepo.kt.settings.WindowSettings;

/**
 * Kafka Tool Application
 */
public class KafkaTool extends JFrame implements KafkaConnectionWatcher {

    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
        try {
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException
                | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                KafkaTool kafkaTool = new KafkaTool();
                kafkaTool.launch();
            }
        });

    }

//
////    private TextField bootstrapField;
//
//    private Button btnClusterConnect;
    private final KafkaAdminService adminService;

//
//    private GridPane grid;
//    private TopicsView topicsView;
//
////    private TextField schemaRegistryUrlField;
//
    public KafkaTool() {
        adminService = new KafkaAdminService();
        adminService.watch(this);
        buildComponents();
    }

//
//    private void connect() {
////        var boostrapServer = bootstrapField.textProperty().get().trim();
////        adminService.connect(boostrapServer, status -> {
////            if (status == BrokerStatus.CONNECTED) {
////                var kafkaSettings = Settings.kafka();
////                kafkaSettings.bootStrapServers(boostrapServer);
////                kafkaSettings.schemaRegistryUrl(Optional.ofNullable(schemaRegistryUrlField.textProperty()
////                                                                                          .get())
////                                                        .orElse("")
////                                                        .trim());
////                kafkaSettings.save();
////            }
////            updateButton();
////        });
//
//    }
//
//    private TextField addTextField(String title) {
//        int row = grid.getRowCount();
//
//        var textLabel = new Text(title);
//        textLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
//        grid.add(textLabel, 0, row);
//
//        TextField textField = new TextField();
//        textField.textProperty().addListener((observable, oldValue, newValue) -> this.updateButton());
//        textField.setMinWidth(256);
//        grid.add(textField, 1, row);
//
//        return textField;
//    }
//
//    private Button addButton(String label, Runnable callback) {
//        int row = grid.getRowCount();
//
//        var btn = new Button(label);
//        btn.setOnAction(e -> callback.run());
//        GridPane.setHgrow(btn, Priority.ALWAYS);
//        grid.add(btn, 1, row);
//        GridPane.setColumnSpan(btn, 2);
//        return btn;
//    }
//
    private void buildComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setTitle("Kafka Tool");
        GridBagFormBuilder screenBuilder = ScreenBuilder.grid();
        screenBuilder.newLabel("Broker");
        screenBuilder.hGrow(screenBuilder.newComboBox(new KafkaBrokerModel()), 1.0);

//        brokerCombo.setItems(observableList(Settings.kafka().getBrokers()));
//        brokerCombo.setCellFactory(items -> new ListCell<KafkaBroker>() {
//            @Override
//            public void updateItem(KafkaBroker item, boolean empty) {
//                if (!empty) {
//                    setText(item.getName());
//                } else {
//                    setText("-");
//                }
//            }
//        });
//        GridPane.setHgrow(brokerCombo, Priority.ALWAYS);
//        grid.add(brokerCombo, 1, 0);
        JButton btnConfigBrokers = screenBuilder.newButton("Config");
        btnConfigBrokers.addActionListener(e -> {
            BrokerConfigurationDialog brokerConfigurationWindow = new BrokerConfigurationDialog(this);
            brokerConfigurationWindow.setVisible(true);
        });
        screenBuilder.newLine();
        screenBuilder.skipCell();

//        btnConfigureBrokers.setMinWidth(96);
//        grid.add(btnConfigureBrokers, 2, 0);
//
////        bootstrapField = addTextField("Boostrap Servers");
////        schemaRegistryUrlField = addTextField("Schema Registry URL");

        JButton btnClusterConnect = screenBuilder.newButton("Connect", 2);
        screenBuilder.newLine();
//        JButton btnClusterConnect = new JButton("Connect"); // addButton("Connect", this::connect);

//        topicsView = new TopicsView(adminService);
//        grid.add(topicsView, 0, 3);
//        setColumnSpan(topicsView, 3);
//        setVgrow(topicsView, Priority.ALWAYS);
//
        JButton btnRefreshTopics = screenBuilder.newButton("Refresh", 3);

//        btnRefreshTopics.setOnAction(e -> topicsView.update());
//        grid.add(btnRefreshTopics, 0, 4);
//        setColumnSpan(btnRefreshTopics, 3);
//
//        var currentSettings = Settings.kafka();
////        this.bootstrapField.textProperty().set(currentSettings.bootStrapServers());
////        this.schemaRegistryUrlField.textProperty().set(currentSettings.schemaRegistryUrl());
//        updateButton();
//
        WindowSettings mainWindows = Settings.ui().getMainWindow();
        setSize(mainWindows.getWidth(), mainWindows.getHeight());
//        var scene = new Scene(grid, mainWindows.getWidth(), mainWindows.getHeight());
//
//        stage.setScene(scene);
//        stage.getIcons().add(new Image(KafkaTool.class.getResourceAsStream("/kafka.png")));
//        stage.widthProperty().addListener((obs, oldValue, newValue) -> {
//            topicsView.setPrefWidth(newValue.doubleValue());
//            brokerCombo.setPrefWidth(grid.getCellBounds(1, 0).getWidth());
//            btnClusterConnect.setPrefWidth(grid.getCellBounds(1, 1).getWidth() +
//                    grid.getCellBounds(1, 2).getWidth());
//            btnRefreshTopics.setPrefWidth(newValue.doubleValue());
//
//            var uiSettings = Settings.ui();
//            uiSettings.getMainWindow().setWidth(newValue.intValue());
//            uiSettings.save();
//        });
//        stage.heightProperty().addListener((obs, oldValue, newValue) -> {
//           
//        });
//        stage.show();
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Settings.updateUi(settings -> {
                    settings.getMainWindow().setHeight(getHeight());
                    settings.getMainWindow().setWidth(getWidth());
                });
            }
        });

        getContentPane().add(screenBuilder.build());
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage(KafkaTool.class.getResource("/kafka.png")));
    }

//
//    private void updateButton() {
//        statusChanged(this.adminService.getStatus());
//    }
//
//    @Override
//    public void stop() throws Exception {
//        this.adminService.close();
//    }
//
//    @Override
    public void statusChanged(BrokerStatus status) {
        switch (status) {
            case IDLE:
//                this.btnClusterConnect.setDisable(Optional.ofNullable(this.bootstrapField.textProperty().get())
//                                                          .orElse("")
//                                                          .isBlank());
                break;
            case CONNECTED:
//                this.btnClusterConnect.setDisable(true);
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + status);
        }
    }

    public void launch() {
        setVisible(true);
    }

}