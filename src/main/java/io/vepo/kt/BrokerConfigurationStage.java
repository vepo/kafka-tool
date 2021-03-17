package io.vepo.kt;

import java.awt.Frame;

import javax.swing.JDialog;

import io.vepo.kt.ScreenBuilder.GridBagFormBuilder;

@SuppressWarnings("serial")
public class BrokerConfigurationStage extends JDialog {
    public BrokerConfigurationStage(Frame owner) {
        super(owner, true);
        configure();
    }

    private void configure() {
        GridBagFormBuilder builder = ScreenBuilder.grid();
        builder.newLabel("Name");
        builder.hGrow(builder.newTextField(), 1.0);
        builder.newLine();
        builder.newLabel("Bootstap Servers");
        builder.hGrow(builder.newTextField(), 1.0);
        builder.newLine();
        builder.newLabel("Schema Registry URL");
        builder.hGrow(builder.newTextField(), 1.0);
        add(builder.build());
        pack();
    }
}
