package io.vepo.kafka.tool.settings;

public enum ValueSerializer {
    JSON("Json", false), AVRO("AVRO", true), PROTOBUF("Protobuf", true), PLAIN_TEXT("Plain Text", false);

    private final boolean requireSchemaRegistry;
    private String serializer;

    private ValueSerializer(String serializer, boolean requireSchemaRegistry) {
        this.serializer = serializer;
        this.requireSchemaRegistry = requireSchemaRegistry;
    }

    public boolean isRequireSchemaRegistry() {
        return requireSchemaRegistry;
    }

    public String serializer() {
        return serializer;
    }
}
