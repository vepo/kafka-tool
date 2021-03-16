package io.vepo.kt.settings;

import java.util.Objects;

public class WindowSettings {
    private int width;
    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width);
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
        WindowSettings other = (WindowSettings) obj;
        return height == other.height && width == other.width;
    }

    @Override
    public String toString() {
        return String.format("WindowSettings [width=%s, height=%s]", width, height);
    }

}
