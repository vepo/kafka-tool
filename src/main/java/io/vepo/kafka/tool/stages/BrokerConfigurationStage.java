package io.vepo.kafka.tool.stages;

import static io.vepo.kafka.tool.controls.builders.ResizePolicy.fixedSize;
import static io.vepo.kafka.tool.controls.builders.ResizePolicy.grow;
import static javafx.collections.FXCollections.observableArrayList;

import io.vepo.kafka.tool.controls.base.AbstractKafkaToolStage;
import io.vepo.kafka.tool.controls.builders.ScreenBuilder;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.Settings;
import io.vepo.kafka.tool.settings.WindowSettings;
import javafx.application.Platform;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

public class BrokerConfigurationStage extends AbstractKafkaToolStage {

    public BrokerConfigurationStage(Stage owner) {
        super("kafkaBrokerConfig", owner, true, new WindowSettings(670, 512));
        setTitle("Kafka Brokers");
        var gridBuilder = ScreenBuilder.grid();
        gridBuilder.addText("Name");
        var txtName = gridBuilder.addTextField();
        gridBuilder.newLine().addText("Boostrap Servers");
        var txtBootstrapServers = gridBuilder.addTextField();
        gridBuilder.newLine().addText("Schema Registry URL");
        var txtSchemaRegistryUrl = gridBuilder.addTextField();
        var btnAdd = gridBuilder.newLine().skipCell().addButton("Add");

        var brokers = Settings.kafka().getBrokers();
        var data = observableArrayList(brokers);
        var dataTable = gridBuilder.newLine()
                                   .<KafkaBroker>addTableView(2)
                                   .<String>withColumn("Name")
                                   .fromProperty("name")
                                   .editable(TextFieldTableCell.<KafkaBroker>forTableColumn(),
                                             (broker, value) -> Settings.updateKafka(kafka -> {
                                                 broker.setName(value);
                                                 kafka.setBrokers(brokers);
                                             }))
                                   .notResizable()
                                   .reorderable()
                                   .resizePolicy(fixedSize(128))
                                   .add()
                                   .<String>withColumn("Boostrap Servers")
                                   .fromProperty("bootStrapServers")
                                   .editable(TextFieldTableCell.<KafkaBroker>forTableColumn(),
                                             (broker, value) -> Settings.updateKafka(kafka -> {
                                                 broker.setBootStrapServers(value);
                                                 kafka.setBrokers(brokers);
                                             }))
                                   .resizePolicy(grow(1))
                                   .notResizable()
                                   .notReorderable()
                                   .add()
                                   .<String>withColumn("Schema Registry URL")
                                   .fromProperty("schemaRegistryUrl")
                                   .editable(TextFieldTableCell.<KafkaBroker>forTableColumn(),
                                             (broker, value) -> Settings.updateKafka(kafka -> {
                                                 broker.setSchemaRegistryUrl(value);
                                                 kafka.setBrokers(brokers);
                                             }))
                                   .resizePolicy(fixedSize(128))
                                   .notResizable()
                                   .notReorderable()
                                   .add()
                                   .withButtons("Actions")
                                   .button("Delete",
                                           broker -> {
                                               brokers.remove(broker);
                                               Platform.runLater(() -> data.remove(broker));
                                               Settings.updateKafka(kafka -> kafka.getBrokers().remove(broker));
                                           })
                                   .resizePolicy(fixedSize(64))
                                   .add()
                                   .build();

        dataTable.setItems(data);
        setScene(gridBuilder.build());
        btnAdd.setOnAction(e -> {
            var broker = new KafkaBroker(txtName.textProperty().get(),
                                         txtBootstrapServers.textProperty().get(),
                                         txtSchemaRegistryUrl.textProperty().get());
            Settings.updateKafka(kafka -> kafka.getBrokers().add(broker));
            Platform.runLater(() -> {
                txtName.textProperty().set("");
                txtBootstrapServers.textProperty().set("");
                txtSchemaRegistryUrl.textProperty().set("");
                dataTable.getItems().add(broker);
            });
        });
    }
    
    

}
