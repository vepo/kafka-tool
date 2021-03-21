package io.vepo.kt.settings;

import java.util.Objects;

public class WindowSettings implements Cloneable {
    private int width;
    private int height;

    public WindowSettings() {
    }

    public WindowSettings(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WindowSettings that = (WindowSettings) o;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return String.format("WindowSettings [width=%s, height=%s]", width, height);
    }

    @Override
    public WindowSettings clone() {
        try {
            return (WindowSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone!", e);
        }
    }

}
