package dev.vepo.kafka.tool.settings;

public enum ValueSerializer {
    JSON("Json", false), AVRO("AVRO", true), PROTOBUF("Protobuf", true);

    private final boolean requireSchemaRegistry;
    private String serializer;

    private ValueSerializer(String serializer, boolean requireSchemaRegistry) {
        this.serializer = serializer;
        this.requireSchemaRegistry = requireSchemaRegistry;
    }

    public String serializer() {
        return serializer;
    }

    public boolean isRequireSchemaRegistry() {
        return requireSchemaRegistry;
    }
}
