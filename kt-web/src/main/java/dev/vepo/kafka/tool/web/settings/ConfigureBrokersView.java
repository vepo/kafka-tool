package dev.vepo.kafka.tool.web.settings;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import dev.vepo.kafka.tool.core.model.KafkaBroker;
import dev.vepo.kafka.tool.web.KafkaBrokerEntity;
import dev.vepo.kafka.tool.web.UserSession;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("/configure/broker")
public class ConfigureBrokersView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(ConfigureBrokersView.class);

    @Inject
    public ConfigureBrokersView(UserSession session) {
        logger.info("Creating configure brokers view... {}", session);
        add(new H1("Configure Brokers"));

        var btnBar = new HorizontalLayout();
        btnBar.add(new Button("New", e -> e.getSource().getUI().ifPresent(ui -> ui.navigate("/configure/broker/new"))));
        add(btnBar);

        var brokersGrid = new Grid<>(KafkaBrokerEntity.findAllBrokers());
        brokersGrid.addColumn(KafkaBroker::getName).setHeader("Name");
        brokersGrid.addColumn(KafkaBroker::getBootStrapServers).setHeader("Bootstrap Servers");
        brokersGrid.addColumn(KafkaBroker::getSchemaRegistryUrl).setHeader("Schema Registry URL");
        add(brokersGrid);
    }
}
