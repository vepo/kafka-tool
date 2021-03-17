package io.vepo.kt.settings;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class WindowSettings {
    @JsonProperty("width")
    private int width;
    @JsonProperty("height")
    private int height;

    public WindowSettings() {
    }

    public WindowSettings(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public int height() {
        return height;
    }

    public void height(int height) {
        this.height = height;
    }

    public int width() {
        return width;
    }

    public void width(int width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WindowSettings that = (WindowSettings) o;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WindowSettings.class.getSimpleName() + "[", "]")
                .add("width=" + width)
                .add("height=" + height)
                .toString();
    }
}
