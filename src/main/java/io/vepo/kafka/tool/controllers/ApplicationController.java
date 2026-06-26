package io.vepo.kafka.tool.controllers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.inspect.ConnectionResult;
import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.inspect.KafkaAdminService.BrokerStatus;
import io.vepo.kafka.tool.inspect.RecordBrowseService;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.service.SettingsService;
import io.vepo.kafka.tool.stages.BrokerConfigurationStage;
import io.vepo.kafka.tool.stages.RecordBrowseStage;
import io.vepo.kafka.tool.stages.TopicSubscribeStage;
import javafx.stage.Stage;

public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final KafkaAdminService adminService;
    private final SettingsService settingsService;
    private Consumer<BrokerStatus> connectionListener;
    private Runnable disconnectionListener;
    private ConsumerGroupsController consumerGroupsController;

    public ApplicationController() {
        this.adminService = new KafkaAdminService();
        this.settingsService = new SettingsService();
    }

    public void connect(KafkaBroker broker, Consumer<ConnectionResult> resultCallback) {
        adminService.connect(broker, result -> {
            logger.info("Connect result: success={}, message={}", result.success(), result.message());
            javafx.application.Platform.runLater(() -> {
                if (result.success() && connectionListener != null) {
                    connectionListener.accept(result.status());
                }
                if (resultCallback != null) {
                    resultCallback.accept(result);
                }
            });
        });
    }

    public ClusterConnectController createClusterConnectController() {
        return new ClusterConnectController(settingsService, this);
    }

    public ConsumerGroupsController createConsumerGroupsController() {
        if (consumerGroupsController == null) {
            consumerGroupsController = new ConsumerGroupsController(adminService, this::disconnect);
        }
        return consumerGroupsController;
    }

    public TopicsController createTopicsController() {
        return new TopicsController(adminService, settingsService, this::openSubscribeStage, this::openBrowseStage,
                                    this::disconnect);
    }

    public void disconnect() {
        adminService.disconnect(status -> javafx.application.Platform.runLater(() -> {
            if (disconnectionListener != null) {
                disconnectionListener.run();
            }
        }));
    }

    public KafkaAdminService getAdminService() {
        return adminService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void onMainWindowResize(int width, int height) {
        settingsService.updateUi(ui -> {
            ui.getMainWindow().setWidth(width);
            ui.getMainWindow().setHeight(height);
        });
    }

    public void openBrokerConfiguration(Stage owner) {
        var controller = new BrokerConfigController(settingsService, this);
        new BrokerConfigurationStage(controller, owner).showAndWait();
    }

    private void openBrowseStage(String topic, Stage owner) {
        var browseController = new RecordBrowseController(settingsService, new RecordBrowseService(adminService),
                                                          adminService.connectedBroker(), topic);
        new RecordBrowseStage(browseController, owner).show();
    }

    private void openSubscribeStage(String topic, Stage owner) {
        var subscribeController = new SubscribeController(settingsService, adminService.connectedBroker(), topic);
        new TopicSubscribeStage(subscribeController, owner).show();
    }

    public void setConnectionListener(Consumer<BrokerStatus> connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void setDisconnectionListener(Runnable disconnectionListener) {
        this.disconnectionListener = disconnectionListener;
    }

    public void shutdown() {
        if (consumerGroupsController != null) {
            consumerGroupsController.shutdown();
        }
        adminService.close();
    }

    public void testConnection(KafkaBroker broker, Consumer<ConnectionResult> callback) {
        adminService.testConnection(broker, result -> javafx.application.Platform.runLater(() -> {
            if (callback != null) {
                callback.accept(result);
            }
        }));
    }

}
