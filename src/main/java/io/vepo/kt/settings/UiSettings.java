package io.vepo.kt.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class UiSettings implements Settings<UiSettings>, Cloneable {
    static final String UI_SETTINGS_FILE = "ui-properties.json";

    @JsonProperty("mainWindow")
    private WindowSettings mainWindow;

    public UiSettings() {
    }

    public UiSettings(WindowSettings mainWindow) {
        this.mainWindow = mainWindow;
    }

    public WindowSettings mainWindow() {
        return mainWindow;
    }

    public void mainWindow(WindowSettings mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Override
    public void save() {
        Settings.save(UI_SETTINGS_FILE, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UiSettings that = (UiSettings) o;
        return Objects.equals(mainWindow, that.mainWindow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainWindow);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UiSettings.class.getSimpleName() + "[", "]")
                .add("mainWindow=" + mainWindow)
                .toString();
    }

    @Override
    public UiSettings clone() {
        try {
            return (UiSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cannot clone!", e);
        }
    }
}
