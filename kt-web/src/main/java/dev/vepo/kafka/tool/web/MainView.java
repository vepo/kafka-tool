package dev.vepo.kafka.tool.web;

import java.util.ArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.h2.engine.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);

    @Inject
    public MainView(UserSession session) {
        logger.info("Creating main view... {}", session);
        add(new H1("Kafka Tool"));
        var comboBox = new ComboBox<String>("Cluster");
        comboBox.setAllowCustomValue(true);
        var clusters = new ArrayList<String>();
        comboBox.setItems(clusters);

        var btnConfigure = new Button("Configure", e -> {
            logger.info("Configure Button clicked!");
            e.getSource()
                    .getUI()
                    .ifPresent(ui -> {
                        ui.navigate("/configure/broker");
                    });
        });
        add(new HorizontalLayout(Alignment.BASELINE, comboBox, btnConfigure));

        var btnConnect = new Button("Connect", e -> logger.info("Connect Button clicked!"));
        add(btnConnect);
        setWidth("100%");
        setSizeFull();

    }
}
