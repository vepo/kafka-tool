package io.vepo.kafka.tool.settings;

public enum ValueSerializer {
    JSON("Json"), AVRO("AVRO"), PROTOBUF("Protobuf");

    private String serializer;

    private ValueSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String serializer() {
        return serializer;
    }
}
