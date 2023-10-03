package dev.vepo.kafka.tool.web;

import com.vaadin.quarkus.annotation.VaadinSessionScoped;

import java.io.Serializable;

@VaadinSessionScoped
public class UserSession implements Serializable {
    private String broker;

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }
}
