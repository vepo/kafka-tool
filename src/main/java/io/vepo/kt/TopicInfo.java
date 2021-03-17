package io.vepo.kt;

public final record TopicInfo(String name, boolean internal) {

    public String getName() {
        return name;
    }

    public boolean isInternal() {
        return internal;
    }
}
