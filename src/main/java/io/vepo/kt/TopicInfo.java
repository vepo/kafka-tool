package io.vepo.kt;

import java.util.Objects;
import java.util.StringJoiner;

public final class TopicInfo {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicInfo topicInfo = (TopicInfo) o;
        return internal == topicInfo.internal && Objects.equals(name, topicInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, internal);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TopicInfo.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("internal=" + internal)
                .toString();
    }
}
