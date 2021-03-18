package io.vepo.kt.components;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;

import io.vepo.kt.settings.Settings;
import io.vepo.kt.settings.WindowSettings;

public abstract class KafkaToolDialog extends JDialog {

    private static final Dimension MINIMUN_SIZE = new Dimension(512, 512);
    private static final long serialVersionUID = 6155439184013314269L;

    public KafkaToolDialog(String dialogName, Frame owner, boolean modal) {
        super(owner, modal);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Settings.updateUi(settings -> {
                    Dimension size = e.getComponent().getSize();
                    settings.getDialogs().put(dialogName, new WindowSettings(size.width, size.height));
                });
            }
        });
        add(build());
        setMinimumSize(MINIMUN_SIZE);
        Map<String, WindowSettings> dialogs = Settings.ui().getDialogs();
        if (dialogs.containsKey(dialogName)) {
            WindowSettings window = dialogs.get(dialogName);
            setSize(new Dimension(window.getWidth(),
                                  window.getHeight()));
        } else {
            setSize(MINIMUN_SIZE);
        }
    }

    protected abstract JComponent build();
}
