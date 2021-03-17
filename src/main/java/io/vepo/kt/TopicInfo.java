package io.vepo.kt;

import java.util.Objects;

public class TopicInfo {

    private final String name;
    private final boolean internal;

    public TopicInfo(String name, boolean internal) {
        this.name = name;
        this.internal = internal;
    }

    public String getName() {
        return name;
    }

    public boolean isInternal() {
        return internal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(internal, name);
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
        TopicInfo other = (TopicInfo) obj;
        return internal == other.internal && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return String.format("TopicInfo [name=%s, internal=%s]", name, internal);
    }

}
