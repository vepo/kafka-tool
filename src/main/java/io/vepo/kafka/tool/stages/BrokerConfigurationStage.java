package io.vepo.kafka.tool.stages;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;

import io.vepo.kafka.tool.controllers.BrokerConfigController;
import io.vepo.kafka.tool.controllers.BrokerConfigController.BrokerValidationException;
import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import static io.vepo.kafka.tool.controls.builders.UI.grid;
import io.vepo.kafka.tool.controls.helpers.TableActionIcons;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.KafkaBrokerValidator;
import io.vepo.kafka.tool.settings.WindowSettings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BrokerConfigurationStage extends AbstractKafkaToolStage {

    private static void applyTableEdit(BrokerConfigController controller, KafkaBroker broker, Runnable applyChange,
                                       Label validationLabel, TableView<KafkaBroker> dataTable, TextField txtName, TextField txtBootstrapServers,
                                       TextField txtSchemaRegistryUrl) {
        try {
            controller.applyBrokerEdit(broker, applyChange);
            clearValidation(validationLabel, txtName, txtBootstrapServers, txtSchemaRegistryUrl);
            controller.viewMessage().showSuccess("Broker \"" + broker.getName() + "\" updated.");
            dataTable.refresh();
        } catch (BrokerValidationException e) {
            controller.viewMessage().showError(e.getMessage());
            showValidation(validationLabel, broker, controller, e.getMessage(), txtName, txtBootstrapServers,
                           txtSchemaRegistryUrl);
            dataTable.refresh();
        }
    }

    private static void clearValidation(Label validationLabel, TextField txtName, TextField txtBootstrapServers,
                                        TextField txtSchemaRegistryUrl) {
        validationLabel.setText("");
        validationLabel.setManaged(false);
        validationLabel.setVisible(false);
        setFieldInvalid(txtName, false);
        setFieldInvalid(txtBootstrapServers, false);
        setFieldInvalid(txtSchemaRegistryUrl, false);
    }

    private static KafkaBroker draftBroker(TextField txtName, TextField txtBootstrapServers,
                                           TextField txtSchemaRegistryUrl) {
        return new KafkaBroker(txtName.getText().trim(), txtBootstrapServers.getText().trim(),
                               txtSchemaRegistryUrl.getText().trim());
    }

    private static void refreshAddButtonState(BrokerConfigController controller, TextField txtName,
                                              TextField txtBootstrapServers, TextField txtSchemaRegistryUrl, Button btnAdd) {
        btnAdd.setDisable(!controller.validateDraft(draftBroker(txtName, txtBootstrapServers, txtSchemaRegistryUrl))
                                     .valid());
    }

    private static void setFieldInvalid(TextField field, boolean invalid) {
        if (field == null) {
            return;
        }
        if (invalid) {
            if (!field.getStyleClass().contains("invalid")) {
                field.getStyleClass().add("invalid");
            }
        } else {
            field.getStyleClass().remove("invalid");
        }
    }

    private static void showValidation(Label validationLabel, KafkaBroker broker, BrokerConfigController controller,
                                       String message, TextField txtName, TextField txtBootstrapServers, TextField txtSchemaRegistryUrl) {
        validationLabel.setText(message);
        validationLabel.setManaged(true);
        validationLabel.setVisible(true);
        if (broker != null) {
            setFieldInvalid(txtName,
                            !KafkaBrokerValidator.validateName(broker.getName(), broker, controller.getBackingBrokers()).valid());
            setFieldInvalid(txtBootstrapServers,
                            !KafkaBrokerValidator.validateBootstrapServers(broker.getBootStrapServers()).valid());
            setFieldInvalid(txtSchemaRegistryUrl,
                            !KafkaBrokerValidator.validateSchemaRegistryUrl(broker.getSchemaRegistryUrl()).valid());
        }
    }

    public BrokerConfigurationStage(BrokerConfigController controller, Stage owner) {
        super("kafkaBrokerConfig", owner, true, new WindowSettings(670, 512), controller.getSettingsService());
        setTitle("Kafka Brokers");
        var gridBuilder = grid()
                                .withViewHeader("Kafka brokers",
                                                "Add and edit cluster profiles. Test each profile before connecting.");
        gridBuilder.getViewHeader().bindMessage(controller.viewMessage());
        gridBuilder.addText("Name");
        var txtName = gridBuilder.addTextField();
        gridBuilder.newLine().addText("Bootstrap Servers");
        var txtBootstrapServers = gridBuilder.addTextField();
        gridBuilder.newLine().addText("Schema Registry URL");
        var txtSchemaRegistryUrl = gridBuilder.addTextField();
        txtSchemaRegistryUrl.setPromptText("Optional (e.g. http://localhost:8081)");
        gridBuilder.newLine();
        var validationLabel = gridBuilder.addValidationLabel(2);
        var btnAdd = gridBuilder.newLine().skipCell().addButton("Add");
        var btnTest = gridBuilder.addButton("Test connection");
        btnTest.setOnAction(e -> controller.testConnection(
                                                           draftBroker(txtName, txtBootstrapServers, txtSchemaRegistryUrl),
                                                           result -> {}));

        @SuppressWarnings("unchecked")
        TableView<KafkaBroker>[] dataTableRef = new TableView[1];
        dataTableRef[0] = gridBuilder.newLine()
                                     .<KafkaBroker>addTableView(2)
                                     .withStringColumn("Name")
                                     .fromString(KafkaBroker::getName)
                                     .editableString((broker, value) -> applyTableEdit(controller, broker, () -> broker.setName(value),
                                                                                       validationLabel, dataTableRef[0], txtName, txtBootstrapServers,
                                                                                       txtSchemaRegistryUrl))
                                     .notResizable()
                                     .reorderable()
                                     .resizePolicy(fixedSize(128))
                                     .add()
                                     .withStringColumn("Bootstrap Servers")
                                     .fromString(KafkaBroker::getBootStrapServers)
                                     .editableString((broker, value) -> applyTableEdit(controller, broker, () -> broker.setBootStrapServers(value),
                                                                                       validationLabel, dataTableRef[0], null, null, null))
                                     .resizePolicy(grow(1))
                                     .notResizable()
                                     .notReorderable()
                                     .add()
                                     .withStringColumn("Schema Registry URL")
                                     .fromString(KafkaBroker::getSchemaRegistryUrl)
                                     .editableString((broker, value) -> applyTableEdit(controller, broker, () -> broker.setSchemaRegistryUrl(value),
                                                                                       validationLabel, dataTableRef[0], null, null, null))
                                     .resizePolicy(fixedSize(128))
                                     .notResizable()
                                     .notReorderable()
                                     .add()
                                     .withButtons("")
                                     .iconButton(TableActionIcons.delete(), "Delete broker", broker -> {
                                         controller.deleteBroker(broker);
                                         controller.viewMessage().showInfo("Broker \"" + broker.getName() + "\" removed.");
                                         clearValidation(validationLabel, txtName, txtBootstrapServers, txtSchemaRegistryUrl);
                                         refreshAddButtonState(controller, txtName, txtBootstrapServers, txtSchemaRegistryUrl, btnAdd);
                                     })
                                     .resizePolicy(fixedSize(40))
                                     .add()
                                     .build();
        var dataTable = dataTableRef[0];

        dataTable.setItems(controller.getBrokers());
        setScene(gridBuilder.build());

        Runnable refreshAddButtonState = () -> refreshAddButtonState(controller, txtName, txtBootstrapServers,
                                                                     txtSchemaRegistryUrl, btnAdd);

        txtName.textProperty().addListener((obs, oldValue, newValue) -> refreshAddButtonState.run());
        txtBootstrapServers.textProperty().addListener((obs, oldValue, newValue) -> refreshAddButtonState.run());
        txtSchemaRegistryUrl.textProperty().addListener((obs, oldValue, newValue) -> refreshAddButtonState.run());
        refreshAddButtonState.run();

        btnAdd.setOnAction(e -> {
            var broker = draftBroker(txtName, txtBootstrapServers, txtSchemaRegistryUrl);
            var result = controller.validateDraft(broker);
            if (!result.valid()) {
                showValidation(validationLabel, broker, controller, result.message(), txtName, txtBootstrapServers,
                               txtSchemaRegistryUrl);
                return;
            }

            controller.addBroker(broker);
            txtName.clear();
            txtBootstrapServers.clear();
            txtSchemaRegistryUrl.clear();
            clearValidation(validationLabel, txtName, txtBootstrapServers, txtSchemaRegistryUrl);
            refreshAddButtonState.run();
        });
    }

}
