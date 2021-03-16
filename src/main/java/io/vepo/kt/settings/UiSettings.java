package io.vepo.kt.settings;

import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class UiSettings extends AbstractSettings {

    private static final String UI_SETTINGS_FILE = "ui-properties.json";
    private static final AtomicReference<UiSettings> INSTANCE = new AtomicReference<UiSettings>();

    public UiSettings() {
        super(UI_SETTINGS_FILE);
    }

    public static UiSettings getInstance() {
        return INSTANCE.updateAndGet(settings -> {
            if (isNull(settings)) {
                settings = loadProperties(UiSettings.class, UI_SETTINGS_FILE).orElseGet(UiSettings::new);
            }
            return settings;
        });
    }

    public static void readAndUpdate(Consumer<UiSettings> consumer) {
        var instance = loadProperties(UiSettings.class, UI_SETTINGS_FILE).orElseGet(UiSettings::new);
        consumer.accept(instance);
        instance.saveProperties();
    }

    private WindowSettings mainWindow;

    public WindowSettings getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(WindowSettings mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainWindow);
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
        return Objects.equals(mainWindow, other.mainWindow);
    }

    @Override
    public String toString() {
        return String.format("UiSettings [mainWindow=%s]", mainWindow);
    }

    @Override
    public UiSettings clone() {
        return (UiSettings) super.clone();
    }

}
