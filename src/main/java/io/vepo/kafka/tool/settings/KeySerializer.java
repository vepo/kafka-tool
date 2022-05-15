package io.vepo.kafka.tool.settings;

public enum KeySerializer {
    INTEGER("int"), STRING("string");

    private String serializer;

    private KeySerializer(String serializer) {
        this.serializer = serializer;
    }

    public String serializer() {
        return serializer;
    }
}
