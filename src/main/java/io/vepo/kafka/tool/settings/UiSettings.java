package io.vepo.kafka.tool.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UiSettings implements Settings<UiSettings>, Cloneable {

    private WindowSettings mainWindow;
    private Map<String, WindowSettings> dialogs;

    public UiSettings() {
        mainWindow = new WindowSettings(512, 512);
        dialogs = new HashMap<String, WindowSettings>();
    }

    @Override
    public UiSettings clone() {
        try {
            return (UiSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cannot clone!", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UiSettings other = (UiSettings) obj;
        return Objects.equals(dialogs, other.dialogs) && Objects.equals(mainWindow, other.mainWindow);
    }

    public Map<String, WindowSettings> getDialogs() {
        return dialogs;
    }

    public WindowSettings getMainWindow() {
        return mainWindow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dialogs, mainWindow);
    }

    public void setDialogs(Map<String, WindowSettings> dialogs) {
        this.dialogs = dialogs;
    }

    public void setMainWindow(WindowSettings mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Override
    public String toString() {
        return String.format("UiSettings [mainWindow=%s, dialogs=%s]", mainWindow, dialogs);
    }
}
