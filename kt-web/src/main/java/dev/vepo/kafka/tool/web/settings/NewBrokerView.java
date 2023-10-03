package dev.vepo.kafka.tool.web.settings;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import dev.vepo.kafka.tool.core.model.KafkaBroker;
import dev.vepo.kafka.tool.web.KafkaBrokerEntity;

@Route("/configure/broker/new")
public class NewBrokerView extends VerticalLayout {

    public NewBrokerView() {
        add(new H1("New Broker"));
        var txtName = new TextField("Name");
        var txtBootstrapServers = new TextField("Bootstrap Servers");
        var txtSchemaRegistryUrl = new TextField("Schema Registry URL");

        var formLayout = new FormLayout();
        formLayout.add(txtName, txtBootstrapServers, txtSchemaRegistryUrl);
        formLayout.setResponsiveSteps(new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2));
        formLayout.setColspan(txtBootstrapServers, 2);
        formLayout.setColspan(txtSchemaRegistryUrl, 2);
        add(formLayout);

        add(new HorizontalLayout(
                new Button("Create", e -> {
                    KafkaBrokerEntity.create(new KafkaBroker(txtName.getValue(),
                            txtBootstrapServers.getValue(),
                            txtSchemaRegistryUrl.getValue()));
                    e.getSource().getUI().ifPresent(ui -> ui.navigate("/configure/broker"));
                }),
                new Button("Cancel", e -> e.getSource()
                        .getUI()
                        .ifPresent(ui -> ui.navigate("/configure/broker")))));
    }
}
