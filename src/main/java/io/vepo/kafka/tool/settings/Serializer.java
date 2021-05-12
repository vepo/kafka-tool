package io.vepo.kafka.tool.settings;

public enum Serializer {
    JSON("Json"), AVRO("AVRO"), PROTOBUF("Protobuf");

    private String ui;

    private Serializer(String ui) {
	this.ui = ui;
    }

    public String ui() {
	return ui;
    }
}
